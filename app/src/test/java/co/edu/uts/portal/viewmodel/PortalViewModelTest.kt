package co.edu.uts.portal.viewmodel

import co.edu.uts.portal.data.Course
import co.edu.uts.portal.data.Faculty
import org.junit.Assert.*
import org.junit.Test

class PortalViewModelTest {
    private val model = PortalViewModel()

    @Test fun rejectsInvalidProfileAndBlocksEnrollment() {
        model.changeCode("no válido")
        model.saveProfile()
        model.chooseCourse(Course.ENGLISH)
        model.enroll()
        val state = model.state.value
        assertNotNull(state.form.nameError)
        assertNotNull(state.form.codeError)
        assertNotNull(state.form.facultyError)
        assertNull(state.student)
        assertFalse(state.canEnroll)
        assertTrue(state.enrollments.isEmpty())
    }

    @Test fun normalizesEmailAndPreservesLeadingZeros() {
        saveProfile()
        assertEquals("maria.jose.00123@uts.edu.co", model.state.value.student?.email)
        assertFalse(model.state.value.canEnroll)
    }

    @Test fun appliesScholarshipBeforeFinancingAndUpdatesHistory() {
        saveProfile()
        model.chooseCourse(Course.ENGLISH)
        model.changeScholarship(true)
        model.changeFinancing(true)
        assertTrue(model.state.value.canEnroll)
        val quote = requireNotNull(model.state.value.quote)
        assertEquals(90_000L, quote.discount)
        assertEquals(4_500L, quote.surcharge)
        assertEquals(94_500L, quote.total)
        model.enroll()

        model.chooseCourse(Course.WEB)
        model.changeScholarship(false)
        assertEquals(252_000L, model.state.value.quote?.total)
        model.changeFinancing(false)
        model.enroll()

        val state = model.state.value
        assertEquals(listOf(Course.WEB, Course.ENGLISH), state.enrollments.map { it.course })
        assertEquals(334_500L, state.total)
        assertEquals(94_500L, state.enrollments.last().quote.total)
        assertEquals("Liquidación #2 registrada.", state.confirmation)
    }

    private fun saveProfile() {
        model.changeName(" María José ")
        model.changeCode("00123")
        model.changeFaculty(Faculty.ENGINEERING)
        model.saveProfile()
    }
}
