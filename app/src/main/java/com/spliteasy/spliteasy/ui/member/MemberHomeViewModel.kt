package com.spliteasy.spliteasy.ui.member

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.spliteasy.data.local.TokenDataStore
import com.spliteasy.spliteasy.data.remote.dto.MemberContributionDto
import com.spliteasy.spliteasy.data.remote.dto.RawUserDto
import com.spliteasy.spliteasy.domain.repository.MemberRepository
import com.spliteasy.spliteasy.util.JwtUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.round

@HiltViewModel
class MemberHomeViewModel @Inject constructor(
    private val repo: MemberRepository,
    private val tokenStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<MemberHomeUiState>(MemberHomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun load(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = MemberHomeUiState.Loading

            // 1) Identidad del usuario: ID desde DataStore; si falta, obtén de JWT
            val storedId = tokenStore.readUserId()
            val token = tokenStore.readToken()
            var currentUserId: Long? = storedId ?: JwtUtils.userId(token)
            var currentUserName: String? = JwtUtils.username(token) // <- reemplaza readUsername

            if (currentUserId == null) {
                _uiState.value = MemberHomeUiState.Empty("Usuario no logueado.")
                return@launch
            }

            // 2) Hogar
            val hh = repo.findMyHouseholdByScanning(currentUserId).getOrElse {
                _uiState.value = MemberHomeUiState.Error(it.message ?: "Error buscando hogar")
                return@launch
            }
            if (hh == null) {
                _uiState.value = MemberHomeUiState.Empty("No perteneces a ningún hogar.")
                return@launch
            }

            val household = repo.fetchHousehold(hh.id).getOrElse {
                _uiState.value = MemberHomeUiState.Error(it.message ?: "Error obteniendo hogar")
                return@launch
            }

            // 3) Miembros
            val membersRaw = repo.fetchHouseholdMembers(hh.id).getOrElse { emptyList() }
            val membersUi = mapMembersToUi(membersRaw)

            // si aún no tenemos nombre, úsalo desde la lista por id
            if (currentUserName.isNullOrBlank()) {
                currentUserName = membersUi.firstOrNull { it.id == currentUserId }?.username
            }

            // 4) Mis contribuciones
            val myContribs = repo.listMyMemberContributions(
                memberId = currentUserId,
                householdId = hh.id
            ).getOrElse { emptyList() }

            val normalized = myContribs.map { it.copy(status = normalizeStatus(it.status)) }
            val (pendingList, paidList) = normalized.partition { it.status == "PENDING" }

            val totalPending = pendingList.sumOf { getAmount(it) }
            val totalPaid    = paidList.sumOf { getAmount(it) }
            val activeCount  = pendingList.size

            // 5) Publica el estado listo (incluye identidad para saludo correcto)
            _uiState.value = MemberHomeUiState.Ready(
                householdName = household.name ?: "Mi hogar",
                householdDescription = household.description ?: "",
                currency = household.currency ?: "PEN",
                members = membersUi,
                totalPending = round2(totalPending),
                totalPaid = round2(totalPaid),
                activeContribsCount = activeCount,
                currentUserId = currentUserId,
                currentUserName = currentUserName
            )
        }
    }

    fun refresh() = load(forceRefresh = true)

    /* ----------------------- helpers ----------------------- */

    private fun normalizeStatus(s: String?): String {
        val v = s?.trim()?.uppercase() ?: "PENDING"
        return if (v == "PAGADO" || v == "PAID") "PAID" else "PENDING"
    }

    private fun round2(x: Double): Double = round(x * 100.0) / 100.0

    /** Lee monto como "monto" (web) o "amount" (android) sin romper DTOs. */
    private fun getAmount(mc: MemberContributionDto): Double {
        // "monto"
        try {
            val f = mc.javaClass.getDeclaredField("monto")
            f.isAccessible = true
            (f.get(mc) as? Number)?.toDouble()?.let { return it }
        } catch (_: Throwable) {}
        // "amount"
        try {
            val f = mc.javaClass.getDeclaredField("amount")
            f.isAccessible = true
            (f.get(mc) as? Number)?.toDouble()?.let { return it }
        } catch (_: Throwable) {}
        return 0.0
    }

    private suspend fun fetchUserSafely(id: Long): RawUserDto? =
        repo.getUser(id).getOrNull()

    private fun List<Any>.looksLikeUsers(): Boolean =
        this.any { it is Map<*, *> && (it["username"] != null || it["email"] != null) }

    private fun Map<*, *>.longOrNull(key: String): Long? =
        (this[key] as? Number)?.toLong()

    private fun Map<*, *>.stringOrEmpty(key: String): String =
        (this[key] as? String) ?: ""

    private suspend fun mapMembersToUi(raw: List<Any>): List<MemberItemUi> {
        if (raw.isEmpty()) return emptyList()

        return if (raw.looksLikeUsers()) {
            raw.mapNotNull { any ->
                val m = any as? Map<*, *> ?: return@mapNotNull null
                val id = m.longOrNull("id") ?: return@mapNotNull null
                val username = m.stringOrEmpty("username").ifBlank {
                    m.stringOrEmpty("email").substringBefore("@", "Usuario")
                }
                val email = m.stringOrEmpty("email")
                val roles = (m["roles"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                MemberItemUi(id = id, username = username, email = email, roles = roles)
            }
        } else {
            val ids = raw.mapNotNull { any ->
                val m = any as? Map<*, *> ?: return@mapNotNull null
                m.longOrNull("userId")
                    ?: (m["user"] as? Map<*, *>)?.longOrNull("id")
                    ?: (m["id"] as? Number)?.toLong()
            }.distinct()

            val resolved = ids.map { uid ->
                viewModelScope.async { uid to fetchUserSafely(uid) }
            }.mapNotNull { it.await() }
                .mapNotNull { (_, user) ->
                    user?.let {
                        val username = it.username?.ifBlank {
                            it.email?.substringBefore("@", "Usuario")
                        } ?: it.email?.substringBefore("@", "Usuario") ?: "Usuario"

                        MemberItemUi(
                            id = it.id,
                            username = username,
                            email = it.email ?: "",
                            roles = it.roles ?: emptyList()
                        )
                    }
                }

            resolved
        }
    }
}
