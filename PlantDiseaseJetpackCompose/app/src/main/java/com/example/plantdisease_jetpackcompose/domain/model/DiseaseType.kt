package com.example.plantdisease_jetpackcompose.domain.model


sealed class DiseaseType(val label: String) {
    object Blight : DiseaseType("Blight")
    object CobRoot : DiseaseType("Cob_Root")
    object CommonRust : DiseaseType("Common_Rust")
    object GrayLeafSpot : DiseaseType("Gray_Leaf_Spot")
    object Healthy : DiseaseType("Healthy")

    companion object {
        fun fromIndex(index: Int): DiseaseType = when(index) {
            0 -> Blight
            1 -> CobRoot
            2 -> CommonRust
            3 -> GrayLeafSpot
            4 -> Healthy
            else -> Healthy
        }

        fun getAllLabels() = listOf("Blight", "Cob_Root", "Common_Rust", "Gray_Leaf_Spot", "Healthy")
    }
}