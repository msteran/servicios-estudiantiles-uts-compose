package co.edu.uts.portal.data

enum class Faculty(val label: String) {
    ENGINEERING("Ingenierías"),
    SOCIOECONOMICS("Ciencias Socioeconómicas"),
}

enum class Course(val label: String, val price: Long) {
    ENGLISH("Inglés Técnico", 180_000),
    WEB("Programación Web", 240_000),
    SECURITY("Ciberseguridad", 320_000),
}

data class Student(
    val name: String,
    val code: String,
    val faculty: Faculty,
    val email: String,
)

data class Quote(val base: Long, val discount: Long, val surcharge: Long) {
    val total: Long get() = base - discount + surcharge
}

data class Enrollment(
    val id: Int,
    val student: Student,
    val course: Course,
    val scholarship: Boolean,
    val financed: Boolean,
    val quote: Quote,
)
