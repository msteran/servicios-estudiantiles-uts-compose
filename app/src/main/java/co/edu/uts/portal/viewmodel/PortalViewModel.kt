package co.edu.uts.portal.viewmodel

import androidx.lifecycle.ViewModel
import co.edu.uts.portal.data.Course
import co.edu.uts.portal.data.Enrollment
import co.edu.uts.portal.data.Faculty
import co.edu.uts.portal.data.Quote
import co.edu.uts.portal.data.Student
import java.text.Normalizer
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ProfileForm(
    val name: String = "",
    val code: String = "",
    val faculty: Faculty? = null,
    val nameError: String? = null,
    val codeError: String? = null,
    val facultyError: String? = null,
)

data class PortalState(
    val form: ProfileForm = ProfileForm(),
    val student: Student? = null,
    val course: Course? = null,
    val scholarship: Boolean = false,
    val financed: Boolean = false,
    val quote: Quote? = null,
    val enrollments: List<Enrollment> = emptyList(),
    val total: Long = 0,
    val confirmation: String? = null,
) {
    val canEnroll: Boolean get() = student != null && quote != null
}

class PortalViewModel : ViewModel() {
    private val _state = MutableStateFlow(PortalState())
    val state: StateFlow<PortalState> = _state.asStateFlow()

    fun changeName(value: String) = _state.update {
        it.copy(form = it.form.copy(name = value, nameError = null))
    }

    fun changeCode(value: String) = _state.update {
        it.copy(form = it.form.copy(code = value, codeError = null))
    }

    fun changeFaculty(value: Faculty) = _state.update {
        it.copy(form = it.form.copy(faculty = value, facultyError = null))
    }

    fun saveProfile() = _state.update { current ->
        val form = current.form
        val name = form.name.trim()
        val code = form.code.trim()
        val slug = Normalizer.normalize(name.lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .replace(Regex("[^a-z0-9]+"), ".")
            .trim('.')
        val validated = form.copy(
            nameError = if (slug.none { it in 'a'..'z' }) "Ingresa un nombre válido." else null,
            codeError = if (code.matches(Regex("[0-9]{1,20}"))) null else "Ingresa de 1 a 20 dígitos.",
            facultyError = if (form.faculty == null) "Selecciona tu facultad." else null,
        )
        val faculty = validated.faculty
        if (validated.nameError != null || validated.codeError != null || faculty == null) {
            return@update current.copy(form = validated)
        }

        // Las liquidaciones conservan el perfil original aunque se edite el registro.
        current.copy(
            form = validated.copy(name = name, code = code),
            student = Student(name, code, faculty, "$slug.$code@uts.edu.co"),
        )
    }

    fun chooseCourse(value: Course) = _state.update {
        it.copy(course = value).withQuote()
    }

    fun changeScholarship(value: Boolean) = _state.update {
        it.copy(scholarship = value).withQuote()
    }

    fun changeFinancing(value: Boolean) = _state.update {
        it.copy(financed = value).withQuote()
    }

    fun enroll() = _state.update { current ->
        val student = current.student ?: return@update current
        val course = current.course ?: return@update current
        val quote = current.quote ?: return@update current
        val entry = Enrollment(
            id = current.enrollments.size + 1,
            student = student,
            course = course,
            scholarship = current.scholarship,
            financed = current.financed,
            quote = quote,
        )
        val history = listOf(entry) + current.enrollments
        current.copy(
            enrollments = history,
            total = history.sumOf { it.quote.total },
            confirmation = "Liquidación #${entry.id} registrada.",
        )
    }

    fun clearConfirmation() = _state.update { it.copy(confirmation = null) }

    private fun PortalState.withQuote(): PortalState {
        val base = course?.price ?: return copy(quote = null)
        val discount = if (scholarship) base / 2 else 0L
        val surcharge = if (financed) ((base - discount) * 5 + 50) / 100 else 0L
        return copy(quote = Quote(base, discount, surcharge))
    }
}
