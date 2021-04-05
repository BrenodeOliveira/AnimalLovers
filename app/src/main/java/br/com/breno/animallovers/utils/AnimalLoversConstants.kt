package br.com.breno.animallovers.utils

enum class AnimalLoversConstants (val nome: String) {

    API_ROOT_URL("https://appanimallovers.000webhostapp.com/"),

    DATABASE_ENTITY_ADMIN("admin"),
    DATABASE_ENTITY_CONTA("conta"),
    DATABASE_ENTITY_CONTROL_LOGIN("gerenciamentoDeLogins"),
    DATABASE_NODE_COMMENT("comment"),
    DATABASE_NODE_FRIENDS("amigos"),
    DATABASE_NODE_NOTIFICATIONS("notificacoes"),
    DATABASE_NODE_NOTIFICATIONS_SENT("notificacoesEnviadas"),

    DATABASE_NODE_OWNER("dono"),
    DATABASE_NODE_PET("pet"),
    DATABASE_NODE_PET_ATTR("atributos"),
    DATABASE_NODE_PET_FRIENDS_REQUEST("solicitacoes"),
    DATABASE_NODE_POST_COMMENT("commentsPost"),
    DATABASE_NODE_POST_COMMENT_LIKES("likesComment"),
    DATABASE_NODE_POST_LIKE("likesPost"),
    DATABASE_NODE_REPORT_COMMENT("reporttedComments"),
    DATABASE_NODE_REPORT_POST("reporttedPosts"),
    DATABASE_NODE_TOKEN("deviceToken"),
    CONST_ROOT_POSTS("posts"),

    STORAGE_ROOT("images"),
    STORAGE_ROOT_PROFILE_PHOTOS("profilePicture"),
    STORAGE_ROOT_OWNER_PHOTOS("ownerPicture"),
    STORAGE_PICTURE_EXTENSION(".jpeg"),

    MALE("Macho"),
    FEMALE("Fêmea")
}