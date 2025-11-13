package com.spliteasy.spliteasy.ui.chat

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.foundation.shape.CircleShape

data class ChatMessage(
    val id: Int = (Math.random() * 10000).toInt(),
    val text: String,
    val isFromBot: Boolean = true
)

enum class ChatStep {
    INITIAL,
    ROLE_SELECT,
    REP_HELP,
    MEM_HELP
}


@Composable
fun FloatingChatWidget(modifier: Modifier = Modifier) {
    var isChatOpen by remember { mutableStateOf(false) }

    Box(modifier = modifier.padding(16.dp)) {
        if (isChatOpen) {
            HelpAssistantScreen(
                modifier = Modifier
                    .fillMaxHeight(0.7f)
                    .width(320.dp),
                onClose = { isChatOpen = false }
            )
        } else {
            FloatingActionButton(
                onClick = { isChatOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Filled.SupportAgent,
                    contentDescription = "Abrir Soporte"
                )
            }
        }
    }
}

@Composable
fun HelpAssistantScreen(modifier: Modifier = Modifier, onClose: () -> Unit) {

    var currentStep by remember { mutableStateOf(ChatStep.INITIAL) }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    fun sendMessage(text: String, isFromBot: Boolean = true, newStep: ChatStep) {
        messages.add(ChatMessage(text = text, isFromBot = isFromBot))
        currentStep = newStep
        coroutineScope.launch {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    LaunchedEffect(Unit) {
        messages.add(
            ChatMessage(
                text = "¡Hola! 👋 Soy el asistente de SplitEasy. " +
                        "Estoy aquí para ayudarte a resolver tus dudas."
            )
        )
        kotlinx.coroutines.delay(800)
        currentStep = ChatStep.ROLE_SELECT
    }

    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.large),
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            ChatHeader(
                step = currentStep,
                onClose = onClose,
                onBack = {
                    currentStep = ChatStep.ROLE_SELECT
                    messages.add(ChatMessage(text = "Elige tu rol para continuar."))
                }
            )
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surface),
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatMessageBubble(message = message)
                }
            }

            AnimatedContent(targetState = currentStep) { step ->
                ChatActions(
                    step = step,
                    onActionSelected = { userMessage, botMessage, nextStep ->
                        messages.add(ChatMessage(text = userMessage, isFromBot = false))
                        sendMessage(botMessage, isFromBot = true, newStep = nextStep)
                    },
                    onRoleSelected = { userMessage, botMessage, nextStep ->
                        messages.add(ChatMessage(text = userMessage, isFromBot = false))
                        messages.add(ChatMessage(text = botMessage))
                        currentStep = nextStep
                    }
                )
            }
        }
    }
}

@Composable
fun ChatHeader(step: ChatStep, onClose: () -> Unit, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (step != ChatStep.INITIAL && step != ChatStep.ROLE_SELECT) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, "Volver",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }

        Text(
            text = "Asistente SplitEasy",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onPrimary
        )
        IconButton(onClick = onClose) {
            Icon(
                Icons.Default.Close, "Cerrar",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    val botShape = RoundedCornerShape(
        topStart = 4.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp
    )
    val userShape = RoundedCornerShape(
        topStart = 20.dp, topEnd = 4.dp, bottomEnd = 20.dp, bottomStart = 20.dp
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromBot) Arrangement.Start else Arrangement.End
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.85f),
            shape = if (message.isFromBot) botShape else userShape,
            color = if (message.isFromBot) MaterialTheme.colorScheme.surfaceContainerHigh
            else MaterialTheme.colorScheme.primary,
            tonalElevation = 1.dp
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (message.isFromBot) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatActions(
    step: ChatStep,
    onRoleSelected: (userMessage: String, botMessage: String, nextStep: ChatStep) -> Unit,
    onActionSelected: (userMessage: String, botMessage: String, nextStep: ChatStep) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 4.dp
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            when (step) {
                ChatStep.ROLE_SELECT -> {
                    BotActionChip("Soy Representante") {
                        onRoleSelected(
                            "Soy Representante",
                            "¡Perfecto! ¿En qué te ayudo?",
                            ChatStep.REP_HELP
                        )
                    }
                    BotActionChip("Soy Miembro") {
                        onRoleSelected(
                            "Soy Miembro",
                            "¡Entendido! ¿Qué duda tienes?",
                            ChatStep.MEM_HELP
                        )
                    }
                }
                ChatStep.REP_HELP -> {
                    BotActionChip("¿Cómo creo un 'Hogar'?") {
                        onActionSelected(
                            "¿Cómo creo un 'Hogar'?",
                            "¡Fácil! \n\n1. Ve a la pantalla 'Mis Hogares'.\n2. Pulsa 'Crear Hogar' y sigue los pasos.",
                            ChatStep.REP_HELP
                        )
                    }
                    BotActionChip("¿Cómo añado un 'Miembro'?") {
                        onActionSelected(
                            "¿Cómo añado un 'Miembro'?",
                            "Claro:\n\n1. Entra a tu 'Hogar'.\n2. Ve a la pestaña 'Miembros'.\n3. Pulsa 'Añadir Miembro' y comparte el enlace.",
                            ChatStep.REP_HELP
                        )
                    }
                    BotActionChip("¿Cómo registro un 'Gasto'?") {
                        onActionSelected(
                            "¿Cómo registro un 'Gasto'?",
                            "Para eso:\n\nDentro de tu 'Hogar', busca la pestaña 'Recibos' o 'Gastos' y usa el botón '+' para registrar uno nuevo.",
                            ChatStep.REP_HELP
                        )
                    }
                    HumanSupportButton()
                }
                ChatStep.MEM_HELP -> {
                    BotActionChip("¿Cómo veo mis deudas?") {
                        onActionSelected(
                            "¿Cómo veo mis deudas?",
                            "¡Sencillo!\n\nEn tu pantalla principal 'Mi Estado', verás un resumen de todas tus deudas y pagos pendientes.",
                            ChatStep.MEM_HELP
                        )
                    }
                    BotActionChip("¿Cómo registro un pago?") {
                        onActionSelected(
                            "¿Cómo registro un pago?",
                            "Para eso:\n\nVe a 'Mis Contribuciones', selecciona el pago que realizaste y sube tu comprobante o recibo.",
                            ChatStep.MEM_HELP
                        )
                    }
                    HumanSupportButton()
                }
                else -> {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
fun BotActionChip(text: String, onClick: () -> Unit) {
    ElevatedSuggestionChip(
        onClick = onClick,
        label = { Text(text, style = MaterialTheme.typography.labelMedium) }
    )
}

@Composable
fun HumanSupportButton() {
    val context = LocalContext.current
    Button(
        onClick = {
            val supportPhoneNumber = "51976360378"
            val message = "Hola SplitEasy, necesito hablar con un humano."
            val url = "https://wa.me/$supportPhoneNumber?text=${Uri.encode(message)}"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                setPackage("com.whatsapp")
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(browserIntent)
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text("Hablar con un humano")
    }
}