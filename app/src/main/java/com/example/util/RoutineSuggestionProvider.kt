package com.example.util

import com.example.data.model.RoutineSuggestion

object RoutineSuggestionProvider {
    val suggestions = listOf(
        // === DAILY SUGGESTIONS (10) ===
        RoutineSuggestion(
            id = "drink_water",
            title = "نوشیدن ۸ لیوان آب",
            shortDescription = "تامین آب کافی برای بدن به بهبود سوخت‌وساز و کارکرد سلول‌ها کمک می‌کند.",
            category = "سلامت و انرژی",
            frequency = "daily",
            suggestedTime = "08:00",
            motivationalText = "امروز هیدراته بمان و به بدنت طراوت ببخش!",
            defaultSnoozeMinutes = 15
        ),
        RoutineSuggestion(
            id = "morning_walk",
            title = "۱۰ دقیقه پیاده‌روی",
            shortDescription = "پیاده‌روی سبک زیر نور خورشید جریان خون را افزایش داده و نشاط‌آور است.",
            category = "سلامت و انرژی",
            frequency = "daily",
            suggestedTime = "07:30",
            motivationalText = "فقط ۱۰ دقیقه قدم بزن تا تفاوت سطح انرژی خود را احساس کنی!",
            defaultSnoozeMinutes = 10
        ),
        RoutineSuggestion(
            id = "read_book",
            title = "۱۵ دقیقه مطالعه",
            shortDescription = "مطالعه مداوم و روزانه ذهن شما را فعال و خلاق نگه می‌دارد.",
            category = "رشد فردی",
            frequency = "daily",
            suggestedTime = "22:00",
            motivationalText = "هر صفحه کتاب، یک گام به سمت فردایی آگاه‌تر است.",
            defaultSnoozeMinutes = 10
        ),
        RoutineSuggestion(
            id = "meditation",
            title = "۵ دقیقه مدیتیشن یا تنفس عمیق",
            shortDescription = "تمرکز روی دم و بازدم اضطراب روزانه را از بین می‌برد.",
            category = "آرامش ذهن",
            frequency = "daily",
            suggestedTime = "14:00",
            motivationalText = "آرامش در میان شلوغی‌های روز با چند تنفس آگاهانه آغاز می‌شود.",
            defaultSnoozeMinutes = 5
        ),
        RoutineSuggestion(
            id = "write_daily_plan",
            title = "نوشتن برنامه روزانه",
            shortDescription = "مکتوب کردن هدف‌های امروز جلوی سردرگمی و اتلاف وقت را می‌گیرد.",
            category = "تمرکز و بهره‌وری",
            frequency = "daily",
            suggestedTime = "08:30",
            motivationalText = "روز خود را طراحی کن تا کارهایت با آرامش و دقت انجام شوند.",
            defaultSnoozeMinutes = 10
        ),
        RoutineSuggestion(
            id = "sleep_hygiene",
            title = "خوابیدن در ساعت مشخص",
            shortDescription = "خواب کافی و منظم در طولانی‌مدت سیستم ایمنی را تقویت می‌کند.",
            category = "سلامت و انرژی",
            frequency = "daily",
            suggestedTime = "23:00",
            motivationalText = "خواب باکیفیت امشب، سوخت فردای پرانرژی توست.",
            defaultSnoozeMinutes = 15
        ),
        RoutineSuggestion(
            id = "clean_room_desk",
            title = "مرتب‌کردن میز یا اتاق",
            shortDescription = "محیط منظم، ذهن شما را برای تمرکز بهتر آماده می‌کند.",
            category = "نظم شخصی",
            frequency = "daily",
            suggestedTime = "18:00",
            motivationalText = "محیط مرتب، افکار شفاف و متمرکزی به ارمغان می‌‌آورد.",
            defaultSnoozeMinutes = 10
        ),
        RoutineSuggestion(
            id = "review_tomorrow_tasks",
            title = "مرور کارهای فردا",
            shortDescription = "بررسی وظایف فردا پیش از خواب، مانع از استرس صبحگاهی می‌شود.",
            category = "نظم شخصی",
            frequency = "daily",
            suggestedTime = "22:30",
            motivationalText = "برنامه‌ریزی امشب، بیداری آسوده‌تری برای صبح خواهد ساخت.",
            defaultSnoozeMinutes = 5
        ),
        RoutineSuggestion(
            id = "social_media_detox",
            title = "۳۰ دقیقه دوری از شبکه‌های اجتماعی",
            shortDescription = "دور شدن موقت از فضای مجازی، به مغز فرصت استراحت و بازسازی می‌دهد.",
            category = "آرامش ذهن",
            frequency = "daily",
            suggestedTime = "20:00",
            motivationalText = "زمان گرانبهای خود را امروز به لحظه حال اختصاص بده.",
            defaultSnoozeMinutes = 15
        ),
        RoutineSuggestion(
            id = "log_daily_mood",
            title = "ثبت حس و حال روزانه",
            shortDescription = "نوشتن احساسات به شما کمک می‌کند الگوهای رفتاری خود را بهتر بشناسید.",
            category = "رشد فردی",
            frequency = "daily",
            suggestedTime = "21:30",
            motivationalText = "احساسات خودت را بشناس تا مسیر رشد شخصی را با همدلی طی کنی.",
            defaultSnoozeMinutes = 10
        ),

        // === WEEKLY SUGGESTIONS (8) ===
        RoutineSuggestion(
            id = "exercise_3x_weekly",
            title = "ورزش ۳ بار در هفته",
            shortDescription = "داشتن فعالیت فیزیکی منظم پایه و اساس سلامت طولانی‌مدت است.",
            category = "سلامت و انرژی",
            frequency = "weekly",
            suggestedTime = "09:00",
            motivationalText = "تحرک بیشتر، شادابی عمیق‌تر قلبی و جسمی!",
            defaultSnoozeMinutes = 15
        ),
        RoutineSuggestion(
            id = "plan_next_week",
            title = "برنامه‌ریزی هفته آینده",
            shortDescription = "تقسیم اهداف بزرگ به قدم‌های هفتگی، تضمین‌کننده پیشرفت مستمر است.",
            category = "تمرکز و بهره‌وری",
            frequency = "weekly",
            suggestedTime = "18:00",
            motivationalText = "با یک برنامه شفاف هفتگی، به استقبال اهداف بزرگتر برو.",
            defaultSnoozeMinutes = 15
        ),
        RoutineSuggestion(
            id = "check_weekly_expenses",
            title = "بررسی هزینه‌های هفته",
            shortDescription = "تحلیل خروجی‌های مالی به کنترل بهتر بودجه کمک می‌کند.",
            category = "مدیریت مالی",
            frequency = "weekly",
            suggestedTime = "19:00",
            motivationalText = "آگاهی از مخارج هفتگی نخستین گام امنیت مالی است.",
            defaultSnoozeMinutes = 10
        ),
        RoutineSuggestion(
            id = "call_family_friend",
            title = "تماس با خانواده یا دوست",
            shortDescription = "حفظ ارتباط با عزیزان به بهبود تعاملات و شادابی روحیه می‌انجامد.",
            category = "روابط و خانواده",
            frequency = "weekly",
            suggestedTime = "17:00",
            motivationalText = "یک تماس ساده می‌تواند روز کسی را زیباتر کند.",
            defaultSnoozeMinutes = 15
        ),
        RoutineSuggestion(
            id = "phone_photo_cleanup",
            title = "مرتب‌سازی فایل‌ها و عکس‌های گوشی",
            shortDescription = "کاهش بار دیجیتالی گوشی، سرعت کاربری و تمرکز شما را افزایش می‌دهد.",
            category = "نظم شخصی",
            frequency = "weekly",
            suggestedTime = "15:00",
            motivationalText = "مرتب کردن دنیای دیجیتال به پاکسازی افکار کمک می‌کند.",
            defaultSnoozeMinutes = 10
        ),
        RoutineSuggestion(
            id = "deep_home_cleaning",
            title = "نظافت کلی اتاق یا خانه",
            shortDescription = "نظافت و شستشو محیط را شاداب و انرژی مثبت را جاری می‌سازد.",
            category = "خانه و زندگی",
            frequency = "weekly",
            suggestedTime = "10:00",
            motivationalText = "پاکیزگی محیط، طراوت‌آور روان است.",
            defaultSnoozeMinutes = 30
        ),
        RoutineSuggestion(
            id = "review_weekly_goals",
            title = "مرور اهداف هفته",
            shortDescription = "ارزیابی این هفته نشان می‌دهد چقدر با اهداف سالانه خود هماهنگ هستید.",
            category = "رشد فردی",
            frequency = "weekly",
            suggestedTime = "20:00",
            motivationalText = "پیشرفت‌های کوچک هفتگی، موفقیت‌های بزرگ خواهند شد.",
            defaultSnoozeMinutes = 10
        ),
        RoutineSuggestion(
            id = "learn_new_skill",
            title = "یادگیری یک مهارت جدید",
            shortDescription = "هر هفته وقت گذاشتن برای مهارت جدید، تخصص و دانایی شما را دگرگون می‌کند.",
            category = "رشد فردی",
            frequency = "weekly",
            suggestedTime = "16:00",
            motivationalText = "هرگز از یاد گرفتن دست نکش؛ جهان همواره در حال تغییر است.",
            defaultSnoozeMinutes = 15
        ),

        // === MONTHLY SUGGESTIONS (7) ===
        RoutineSuggestion(
            id = "review_monthly_goals",
            title = "بررسی اهداف ماهانه",
            shortDescription = "تحلیل مسیر طی شده در سی روز گذشته به شفافیت ادامه راه کمک می‌کند.",
            category = "رشد فردی",
            frequency = "monthly",
            suggestedTime = "18:00",
            motivationalText = "با ارزیابی مستمر، هر ماه بهتر از ماه قبل عمل کن.",
            defaultSnoozeMinutes = 20
        ),
        RoutineSuggestion(
            id = "monthly_budgeting",
            title = "بررسی درآمد و هزینه‌ها",
            shortDescription = "دید کلی به دخل و خرج ماهانه، امنیت مالی آینده شما را فراهم می‌کند.",
            category = "مدیریت مالی",
            frequency = "monthly",
            suggestedTime = "20:00",
            motivationalText = "مدیریت مالی هوشمندانه پایه خوشبختی و آرامش ذهن است.",
            defaultSnoozeMinutes = 15
        ),
        RoutineSuggestion(
            id = "digital_declutter",
            title = "پاکسازی فایل‌ها و برنامه‌های اضافی",
            shortDescription = "سایت‌ها، عکس‌ها و برنامه‌هایی که دیگر استفاده نمی‌کنید را حذف کنید.",
            category = "نظم شخصی",
            frequency = "monthly",
            suggestedTime = "14:00",
            motivationalText = "خودت را از شر زباله‌های دیجیتال خلاص کن.",
            defaultSnoozeMinutes = 15
        ),
        RoutineSuggestion(
            id = "assess_personal_progress",
            title = "مرور پیشرفت شخصی",
            shortDescription = "تامل بر روی عادت‌ها، اشتباهات و دستاوردهای گذشته خود.",
            category = "رشد فردی",
            frequency = "monthly",
            suggestedTime = "21:00",
            motivationalText = "رشد تو بزرگترین دارایی توست؛ قدردان تلاشت باش.",
            defaultSnoozeMinutes = 20
        ),
        RoutineSuggestion(
            id = "plan_next_month",
            title = "برنامه‌ریزی برای ماه بعد",
            shortDescription = "طراحی اهداف جدید و پیش‌بینی رویدادهای ماه آینده.",
            category = "تمرکز و بهره‌وری",
            frequency = "monthly",
            suggestedTime = "11:00",
            motivationalText = "آمادگی از قبل، کلید بهره‌وری کامل است.",
            defaultSnoozeMinutes = 15
        ),
        RoutineSuggestion(
            id = "finish_pending_tasks",
            title = "رسیدگی به کارهای عقب‌افتاده",
            shortDescription = "کارهای کوچکی که مدام به تعویق انداخته‌اید را لیست کرده و انجام دهید.",
            category = "نظم شخصی",
            frequency = "monthly",
            suggestedTime = "16:00",
            motivationalText = "کارهای عقب‌افتاده را تمام کن تا انرژیت آزاد شود.",
            defaultSnoozeMinutes = 30
        ),
        RoutineSuggestion(
            id = "evaluate_monthly_habits",
            title = "بررسی عادت‌های خوب و بد ماه",
            shortDescription = "شناسایی رفتارهای سازنده و مخرب برای بهبود کیفیت مداوم زندگی.",
            category = "رشد فردی",
            frequency = "monthly",
            suggestedTime = "22:00",
            motivationalText = "عادت‌های بد را رها کن و به عادت‌های خوب زندگی بال و پر بده.",
            defaultSnoozeMinutes = 15
        )
    )

    fun getLocalizedTitle(suggestion: RoutineSuggestion, lang: String): String {
        return suggestion.title
    }

    fun getLocalizedDesc(suggestion: RoutineSuggestion, lang: String): String {
        return suggestion.shortDescription
    }
}
