package com.oriundo.lbretaappestudiantil.domain.model

import com.oriundo.lbretaappestudiantil.data.local.models.UserEntity
import com.oriundo.lbretaappestudiantil.data.local.models.ProfileEntity

/**
 * Data class para representar un usuario con su perfil
 */
data class UserWithProfile(
    val user: UserEntity,
    val profile: ProfileEntity,

)