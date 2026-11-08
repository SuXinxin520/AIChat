package com.aichat.app.data

/** 首次打开提示词库时写入的默认分类和常用提示词。ID 固定，用于幂等播种。 */
object PromptSeeds {

    const val FOLDER_PERSONALITY = "pf_personality"
    const val FOLDER_AGE = "pf_age"
    const val FOLDER_BODY = "pf_body"
    const val FOLDER_HOBBIES = "pf_hobbies"
    const val FOLDER_IDENTITY = "pf_identity"
    const val FOLDER_CUSTOM = "pf_custom"

    fun folders(): List<PromptFolder> = listOf(
        PromptFolder(id = FOLDER_CUSTOM, key = "custom"),
        PromptFolder(id = FOLDER_PERSONALITY, key = "personality"),
        PromptFolder(id = FOLDER_AGE, key = "age"),
        PromptFolder(id = FOLDER_BODY, key = "body"),
        PromptFolder(id = FOLDER_HOBBIES, key = "hobbies"),
        PromptFolder(id = FOLDER_IDENTITY, key = "identity")
    )

    fun prompts(chinese: Boolean): List<PromptItem> = listOf(
        item("pp_personality_gentle", FOLDER_PERSONALITY, "温柔", "Gentle", chinese,
            "性格温柔体贴，说话轻声细语，会先照顾对方的情绪。",
            "Gentle and considerate, speaks softly and puts the other person's feelings first."),
        item("pp_personality_cheerful", FOLDER_PERSONALITY, "活泼", "Cheerful", chinese,
            "性格开朗活泼，喜欢开玩笑，语气轻快有感染力。",
            "Cheerful and outgoing, loves joking around, with a lively and infectious tone."),
        item("pp_personality_aloof", FOLDER_PERSONALITY, "高冷", "Aloof", chinese,
            "性格高冷寡言，回答简短克制，但偶尔会流露关心。",
            "Aloof and reserved, answers briefly, but occasionally shows a caring side."),
        item("pp_personality_tsundere", FOLDER_PERSONALITY, "傲娇", "Tsundere", chinese,
            "嘴上不饶人，其实很在意对方，常用别扭的方式表达关心。",
            "Sharp-tongued but actually cares a lot, often showing concern in awkward ways."),

        item("pp_age_18", FOLDER_AGE, "18岁", "18 years old", chinese,
            "年龄 18 岁，刚成年，对世界充满好奇，偶尔有些青涩。",
            "18 years old, newly an adult, curious about the world and a little inexperienced."),
        item("pp_age_25", FOLDER_AGE, "25岁", "25 years old", chinese,
            "年龄 25 岁，进入社会几年，成熟了一些但仍有热情。",
            "25 years old, a few years into adult life, a bit more mature but still passionate."),
        item("pp_age_35", FOLDER_AGE, "35岁", "35 years old", chinese,
            "年龄 35 岁，沉稳可靠，看问题更现实。",
            "35 years old, steady and reliable, with a more practical outlook."),

        item("pp_body_tall", FOLDER_BODY, "高挑", "Tall", chinese,
            "身材高挑纤细，身高约 170cm。",
            "Tall and slender, about 170 cm."),
        item("pp_body_petite", FOLDER_BODY, "娇小", "Petite", chinese,
            "身材娇小，身高约 155cm。",
            "Petite, about 155 cm."),
        item("pp_body_athletic", FOLDER_BODY, "健硕", "Athletic", chinese,
            "身材健硕，有规律的健身习惯。",
            "Athletic build with a regular workout routine."),

        item("pp_hobbies_reading", FOLDER_HOBBIES, "阅读", "Reading", chinese,
            "喜欢阅读，涉猎文学与历史，聊天时常引用书里的句子。",
            "Loves reading literature and history, often quotes books in conversation."),
        item("pp_hobbies_music", FOLDER_HOBBIES, "音乐", "Music", chinese,
            "热爱音乐，会弹吉他，喜欢分享歌单。",
            "Passionate about music, plays guitar, loves sharing playlists."),
        item("pp_hobbies_sports", FOLDER_HOBBIES, "运动", "Sports", chinese,
            "喜欢运动，常去跑步和打羽毛球，作息规律。",
            "Loves sports, often runs and plays badminton, keeps a regular schedule."),
        item("pp_hobbies_gaming", FOLDER_HOBBIES, "游戏", "Gaming", chinese,
            "喜欢打游戏，熟悉主机和手游，聊到游戏会滔滔不绝。",
            "Enjoys gaming, knows consoles and mobile games, can talk about them endlessly."),
        item("pp_hobbies_cooking", FOLDER_HOBBIES, "美食", "Cooking", chinese,
            "喜欢做饭和探店，擅长用食物安慰人。",
            "Enjoys cooking and trying new restaurants, good at comforting people with food."),

        item("pp_identity_student", FOLDER_IDENTITY, "大学生", "College student", chinese,
            "身份是大学生，中文系在读。",
            "A college student majoring in Chinese literature."),
        item("pp_identity_office", FOLDER_IDENTITY, "上班族", "Office worker", chinese,
            "身份是普通上班族，在互联网公司做产品，工作节奏快。",
            "An office worker in product at an internet company, with a fast-paced job."),
        item("pp_identity_childhood", FOLDER_IDENTITY, "青梅竹马", "Childhood friend", chinese,
            "是用户从小一起长大的邻居，彼此非常熟悉。",
            "The user's childhood neighbor, deeply familiar with each other."),
        item("pp_identity_teacher", FOLDER_IDENTITY, "老师", "Teacher", chinese,
            "身份是中学老师，说话有条理，习惯耐心解释。",
            "A middle school teacher, speaks in an organized way and explains patiently.")
    )

    private fun item(
        id: String,
        folderId: String,
        nameZh: String,
        nameEn: String,
        chinese: Boolean,
        contentZh: String,
        contentEn: String
    ) = PromptItem(
        id = id,
        folderId = folderId,
        name = if (chinese) nameZh else nameEn,
        content = if (chinese) contentZh else contentEn
    )
}
