package co.edu.uts.portal.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uts.portal.data.Course
import co.edu.uts.portal.data.Faculty
import co.edu.uts.portal.data.Quote
import co.edu.uts.portal.viewmodel.PortalViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProfileScreen(model: PortalViewModel, modifier: Modifier = Modifier) {
    val state by model.state.collectAsStateWithLifecycle()
    val form = state.form
    FormScreen(modifier) {
        Heading("Perfil académico", "Registra tus datos para habilitar la liquidación de cursos.")
        state.student?.let { student ->
            Highlight("Perfil registrado", student.name, student.email)
        }
        OutlinedTextField(
            value = form.name,
            onValueChange = model::changeName,
            label = { Text("Nombre completo") },
            isError = form.nameError != null,
            supportingText = form.nameError?.let { error -> { Text(error) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.code,
            onValueChange = model::changeCode,
            label = { Text("Código estudiantil") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = form.codeError != null,
            supportingText = form.codeError?.let { error -> { Text(error) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Selector("Facultad", form.faculty, Faculty.entries, Faculty::label, model::changeFaculty, form.facultyError)
        Button(onClick = model::saveProfile, modifier = Modifier.fillMaxWidth()) {
            Text("Guardar perfil")
        }
    }
}

@Composable
fun TuitionScreen(model: PortalViewModel, modifier: Modifier = Modifier, openProfile: () -> Unit) {
    val state by model.state.collectAsStateWithLifecycle()
    FormScreen(modifier) {
        Heading("Matrícula extracurricular", "Elige un curso libre y consulta cuánto pagarías.")
        if (state.student == null) {
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Guarda tu perfil académico para poder liquidar.")
                    TextButton(onClick = openProfile) { Text("Ir al perfil") }
                }
            }
        }
        Selector(
            label = "Curso libre",
            selected = state.course,
            options = Course.entries,
            display = { "${it.label} · ${money(it.price)}" },
            onSelect = model::chooseCourse,
        )
        OutlinedCard(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth()
                    .toggleable(state.scholarship, role = Role.Checkbox, onValueChange = model::changeScholarship)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OptionLabel("Beca de Excelencia UTS", "50 % de descuento", Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                Checkbox(checked = state.scholarship, onCheckedChange = null)
            }
            HorizontalDivider()
            Row(
                Modifier.fillMaxWidth()
                    .toggleable(state.financed, role = Role.Switch, onValueChange = model::changeFinancing)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OptionLabel("Financiación Icetex", "5 % de recargo administrativo", Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                Switch(checked = state.financed, onCheckedChange = null)
            }
        }
        state.quote?.let { QuoteCard(it) }
        Button(onClick = model::enroll, enabled = state.canEnroll, modifier = Modifier.fillMaxWidth()) {
            Text("Liquidar")
        }
        state.confirmation?.let { message ->
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(message, Modifier.weight(1f))
                    TextButton(onClick = model::clearConfirmation) { Text("Cerrar") }
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(model: PortalViewModel, modifier: Modifier = Modifier, openTuition: () -> Unit) {
    val state by model.state.collectAsStateWithLifecycle()
    Column(
        modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Heading("Historial financiero", "Consulta tus liquidaciones extracurriculares.")
        Highlight(
            "Total acumulado a pagar",
            money(state.total),
            "COP · ${state.enrollments.size} liquidaciones",
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(top = 2.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.enrollments.isEmpty()) {
                item {
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Aún no hay liquidaciones.")
                            TextButton(onClick = openTuition) { Text("Ir a matrícula") }
                        }
                    }
                }
            }
            items(state.enrollments, key = { it.id }) { entry ->
                OutlinedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Liquidación #${entry.id}", style = MaterialTheme.typography.labelMedium)
                        Text(entry.course.label, style = MaterialTheme.typography.titleMedium)
                        Text("${entry.student.name} · ${entry.student.code}", style = MaterialTheme.typography.bodyMedium)
                        MoneyRow("Costo base", entry.quote.base)
                        if (entry.scholarship) MoneyRow("Beca UTS", -entry.quote.discount)
                        if (entry.financed) MoneyRow("Recargo Icetex", entry.quote.surcharge)
                        HorizontalDivider()
                        MoneyRow("Total", entry.quote.total, emphasized = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun FormScreen(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

@Composable
private fun Heading(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Highlight(label: String, title: String, detail: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text(detail, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun OptionLabel(title: String, detail: String, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Text(detail, style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> Selector(
    label: String,
    selected: T?,
    options: List<T>,
    display: (T) -> String,
    onSelect: (T) -> Unit,
    error: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.let(display).orEmpty(),
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            readOnly = true,
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(display(option)) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}

@Composable
private fun QuoteCard(quote: Quote) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Resumen en COP", style = MaterialTheme.typography.titleMedium)
            MoneyRow("Costo base", quote.base)
            MoneyRow("Descuento por beca", -quote.discount)
            MoneyRow("Recargo Icetex", quote.surcharge)
            HorizontalDivider()
            MoneyRow("Total a pagar", quote.total, emphasized = true)
        }
    }
}

@Composable
private fun MoneyRow(label: String, amount: Long, emphasized: Boolean = false) {
    val style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, Modifier.weight(1f), style = style)
        Text(money(amount), style = style)
    }
}

private fun money(amount: Long): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }.format(amount)
