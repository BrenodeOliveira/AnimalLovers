package br.com.breno.animallovers.utils

enum class AnimalLoversConstants (val nome: String) {
    DATABASE_ENTITY_ADMIN("admin"),
    DATABASE_NODE_REPORT_COMMENT("reporttedComments"),
    DATABASE_NODE_REPORT_POST("reporttedPosts"),

    DATABASE_ENTITY_CONTA("conta"),
    DATABASE_NODE_FRIENDS("amigos"),
    DATABASE_NODE_OWNER("dono"),
    DATABASE_NODE_PET("pet"),
    DATABASE_NODE_PET_ATTR("atributos"),
    DATABASE_NODE_PET_FRIENDS_REQUEST("solicitacoes"),
    DATABASE_NODE_POST_LIKE("likesPost"),
    DATABASE_NODE_POST_COMMENT("commentsPost"),
    DATABASE_NODE_COMMENT("comment"),
    DATABASE_NODE_POST_COMMENT_LIKES("likesComment"),

    CONST_ROOT_POSTS("posts"),

    STORAGE_ROOT("images"),
    STORAGE_ROOT_PROFILE_PHOTOS("profilePicture"),
    STORAGE_ROOT_OWNER_PHOTOS("ownerPicture"),
    STORAGE_PICTURE_EXTENSION(".jpeg"),

    MALE("Macho"),
    FEMALE("Fêmea")
}