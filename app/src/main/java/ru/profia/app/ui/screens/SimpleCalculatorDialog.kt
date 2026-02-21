package ru.profia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.res.stringResource
import ru.profia.app.R
import ru.profia.app.ui.components.RoundedButton
import ru.profia.app.ui.theme.Pistachio
import kotlin.math.roundToInt

/**
 * Простой калькулятор (модальное окно).
 */
@Composable
fun SimpleCalculatorDialog(
    onDismiss: () -> Unit,
    onApply: ((Double) -> Unit)? = null
) {
    var display by remember { mutableStateOf("0") }
    var currentValue by remember { mutableStateOf(0.0) }
    var pendingOperation by remember { mutableStateOf<String?>(null) }
    var waitingForOperand by remember { mutableStateOf(true) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val history = remember { mutableStateListOf<String>() }

    fun inputDigit(digit: String) {
        if (waitingForOperand) {
            display = digit
            waitingForOperand = false
        } else {
            display = if (display == "0") digit else display + digit
        }
    }

    fun inputDecimal() {
        if (waitingForOperand) {
            display = "0."
            waitingForOperand = false
        } else if (!display.contains(".")) {
            display += "."
        }
    }

    fun inputPercent() {
        val v = display.replace(",", ".").toDoubleOrNull() ?: 0.0
        display = String.format("%.4f", v / 100.0).replace(".", ",").trimEnd('0').trimEnd(',')
        if (display.endsWith(",")) display += "0"
        waitingForOperand = false
    }

    fun clear() {
        display = "0"
        currentValue = 0.0
        pendingOperation = null
        waitingForOperand = true
    }

    fun backspace() {
        if (waitingForOperand) return
        display = if (display.length > 1) {
            display.dropLast(1)
        } else {
            waitingForOperand = true
            "0"
        }
    }

    fun performOperation(nextOp: String) {
        val inputValue = display.replace(",", ".").toDoubleOrNull() ?: 0.0
        if (pendingOperation == null) {
            currentValue = inputValue
        } else {
            when (pendingOperation) {
                "+" -> currentValue += inputValue
                "-" -> currentValue -= inputValue
                "*", "×" -> currentValue *= inputValue
                "/", "÷" -> currentValue = if (inputValue != 0.0) currentValue / inputValue else currentValue
                else -> currentValue = inputValue
            }
        }
        display = String.format("%.2f", currentValue).replace(".", ",")
        pendingOperation = nextOp
        waitingForOperand = true
    }

    fun calculate() {
        if (pendingOperation != null) {
            val inputValue = display.replace(",", ".").toDoubleOrNull() ?: 0.0
            val left = currentValue
            val opSymbol = when (pendingOperation) {
                "+" -> "+"
                "-" -> "−"
                "*", "×" -> "×"
                "/", "÷" -> "÷"
                else -> ""
            }
            when (pendingOperation) {
                "+" -> currentValue += inputValue
                "-" -> currentValue -= inputValue
                "*", "×" -> currentValue *= inputValue
                "/", "÷" -> currentValue = if (inputValue != 0.0) currentValue / inputValue else currentValue
                else -> currentValue = inputValue
            }
            val resultStr = String.format("%.2f", currentValue).replace(".", ",")
            history.add(0, "$left $opSymbol $inputValue = $resultStr")
            if (history.size > 20) history.removeAt(history.lastIndex)
            display = resultStr
            pendingOperation = null
        }
        waitingForOperand = true
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.TopEnd
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .padding(top = 72.dp, end = 16.dp)
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .fillMaxWidth(0.92f)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.padding(top = 0.dp, end = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.close)
                            )
                        }
                    }
                    Text(
                        text = display,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.padding(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CalcButton("7", { inputDigit("7") }, Modifier.weight(1f))
                        CalcButton("8", { inputDigit("8") }, Modifier.weight(1f))
                        CalcButton("9", { inputDigit("9") }, Modifier.weight(1f))
                        CalcButton("+", { performOperation("+") }, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.padding(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CalcButton("4", { inputDigit("4") }, Modifier.weight(1f))
                        CalcButton("5", { inputDigit("5") }, Modifier.weight(1f))
                        CalcButton("6", { inputDigit("6") }, Modifier.weight(1f))
                        CalcButton("−", { performOperation("-") }, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.padding(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CalcButton("1", { inputDigit("1") }, Modifier.weight(1f))
                        CalcButton("2", { inputDigit("2") }, Modifier.weight(1f))
                        CalcButton("3", { inputDigit("3") }, Modifier.weight(1f))
                        CalcButton("×", { performOperation("*") }, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.padding(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CalcButton("0", { inputDigit("0") }, Modifier.weight(1f))
                        CalcButton(",", { inputDecimal() }, Modifier.weight(1f))
                        CalcButton("%", { inputPercent() }, Modifier.weight(1f))
                        CalcButton("÷", { performOperation("/") }, Modifier.weight(1f))
                        CalcButton("=", { calculate() }, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.padding(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CalcButton("C", { clear() }, Modifier.weight(1f))
                        CalcButton("⌫", { backspace() }, Modifier.weight(1f))
                    }
                    if (history.isNotEmpty()) {
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(stringResource(R.string.calculator_history), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            history.take(10).forEach { line ->
                                Text(line, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    if (onApply != null) {
                        Spacer(modifier = Modifier.padding(8.dp))
                        RoundedButton(
                            onClick = {
                                calculate()
                                val result = display.replace(",", ".").toDoubleOrNull() ?: 0.0
                                onApply(result)
                                onDismiss()
                            },
                            text = stringResource(R.string.calculator_apply)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalcButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.padding(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(label)
    }
}
