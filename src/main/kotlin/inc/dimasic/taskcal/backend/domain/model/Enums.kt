package inc.dimasic.taskcal.backend.domain.model

// Names match the Android app's domain enums one-to-one; they travel over the wire by name.

enum class Priority { LOW, MEDIUM, HIGH }

enum class RecurrenceType { NONE, DAILY, WEEKLY, MONTHLY }

enum class MealType { BREAKFAST, LUNCH, DINNER, SNACK }

enum class Gender { MALE, FEMALE, OTHER }
