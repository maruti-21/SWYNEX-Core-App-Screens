package com.focusflow

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(
    name = "focusflow_preferences"
)

private val TASK_EXISTS = booleanPreferencesKey("task_exists")
private val TASK_TITLE = stringPreferencesKey("task_title")
private val TASK_DESCRIPTION = stringPreferencesKey("task_description")
private val TASK_PRIORITY = stringPreferencesKey("task_priority")
private val TASK_CATEGORY = stringPreferencesKey("task_category")
private val TASK_DUE_DATE = stringPreferencesKey("task_due_date")
private val TASK_COMPLETED = booleanPreferencesKey("task_completed")
private val USER_NAME = stringPreferencesKey("user_name")
private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
private val FOCUS_DURATION = stringPreferencesKey("focus_duration")

private const val NOTIFICATION_CHANNEL_ID = "focusflow_reminders"
private const val NOTIFICATION_ID = 1001

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        setContent {
            FocusFlowApp(applicationContext)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "FocusFlow Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "FocusFlow productivity reminders"
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}

@Composable
fun FocusFlowApp(context: Context) {

    val scope = rememberCoroutineScope()

    var currentScreen by remember {
        mutableStateOf("welcome")
    }

    var taskTitle by remember {
        mutableStateOf("")
    }

    var taskDescription by remember {
        mutableStateOf("")
    }

    var taskPriority by remember {
        mutableStateOf("Medium")
    }

    var taskCategory by remember {
        mutableStateOf("Study")
    }

    var taskDueDate by remember {
        mutableStateOf("Today")
    }

    var taskCompleted by remember {
        mutableStateOf(false)
    }

    var hasTask by remember {
        mutableStateOf(false)
    }

    var focusSessions by remember {
        mutableIntStateOf(3)
    }

    var focusMinutes by remember {
        mutableIntStateOf(75)
    }

    var userName by remember {
        mutableStateOf("Focus User")
    }

    var notificationsEnabled by remember {
        mutableStateOf(true)
    }

    var focusDuration by remember {
        mutableStateOf("25")
    }

    LaunchedEffect(Unit) {

        val preferences = context.dataStore.data.first()

        hasTask = preferences[TASK_EXISTS] ?: false
        taskTitle = preferences[TASK_TITLE] ?: ""
        taskDescription = preferences[TASK_DESCRIPTION] ?: ""
        taskPriority = preferences[TASK_PRIORITY] ?: "Medium"
        taskCategory = preferences[TASK_CATEGORY] ?: "Study"
        taskDueDate = preferences[TASK_DUE_DATE] ?: "Today"
        taskCompleted = preferences[TASK_COMPLETED] ?: false
        userName = preferences[USER_NAME] ?: "Focus User"
        notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true
        focusDuration = preferences[FOCUS_DURATION] ?: "25"
    }

    BackHandler(enabled = currentScreen != "welcome") {

        currentScreen = when (currentScreen) {

            "dashboard" -> "welcome"

            "addTask" -> "dashboard"

            "editTask" -> "taskDetails"

            "taskDetails" -> "dashboard"

            "focus" -> "dashboard"

            "progress" -> "dashboard"

            "settings" -> "dashboard"

            else -> "welcome"
        }
    }

    when (currentScreen) {

        "welcome" -> {

            WelcomeScreen {
                currentScreen = "dashboard"
            }
        }

        "dashboard" -> {

            DashboardScreen(

                hasTask = hasTask,

                taskTitle = taskTitle,

                taskPriority = taskPriority,

                taskCategory = taskCategory,

                taskDueDate = taskDueDate,

                taskCompleted = taskCompleted,

                userName = userName,

                onAddTask = {
                    currentScreen = "addTask"
                },

                onTaskDetails = {
                    currentScreen = "taskDetails"
                },

                onFocus = {
                    currentScreen = "focus"
                },

                onProgress = {
                    currentScreen = "progress"
                },
                onSettings = {
                    currentScreen = "settings"
                }
            )
        }

        "addTask" -> {

            TaskFormScreen(

                screenTitle = "Add New Task",

                buttonText = "Save Task",

                initialTitle = "",

                initialDescription = "",

                initialPriority = "Medium",

                initialCategory = "Study",

                initialDueDate = "Today",

                onSave = {
                        title,
                        description,
                        priority,
                        category,
                        dueDate ->

                    taskTitle = title
                    taskDescription = description
                    taskPriority = priority
                    taskCategory = category
                    taskDueDate = dueDate
                    taskCompleted = false
                    hasTask = true

                    scope.launch {

                        context.dataStore.edit { preferences ->

                            preferences[TASK_EXISTS] = true
                            preferences[TASK_TITLE] = title
                            preferences[TASK_DESCRIPTION] = description
                            preferences[TASK_PRIORITY] = priority
                            preferences[TASK_CATEGORY] = category
                            preferences[TASK_DUE_DATE] = dueDate
                            preferences[TASK_COMPLETED] = false
                        }
                    }

                    currentScreen = "dashboard"
                },

                onBack = {
                    currentScreen = "dashboard"
                }
            )
        }

        "editTask" -> {

            TaskFormScreen(

                screenTitle = "Edit Task",

                buttonText = "Save Changes",

                initialTitle = taskTitle,

                initialDescription = taskDescription,

                initialPriority = taskPriority,

                initialCategory = taskCategory,

                initialDueDate = taskDueDate,

                onSave = {
                        title,
                        description,
                        priority,
                        category,
                        dueDate ->

                    taskTitle = title
                    taskDescription = description
                    taskPriority = priority
                    taskCategory = category
                    taskDueDate = dueDate

                    scope.launch {

                        context.dataStore.edit { preferences ->

                            preferences[TASK_EXISTS] = true
                            preferences[TASK_TITLE] = title
                            preferences[TASK_DESCRIPTION] = description
                            preferences[TASK_PRIORITY] = priority
                            preferences[TASK_CATEGORY] = category
                            preferences[TASK_DUE_DATE] = dueDate
                            preferences[TASK_COMPLETED] = taskCompleted
                        }
                    }

                    currentScreen = "taskDetails"
                },

                onBack = {
                    currentScreen = "taskDetails"
                }
            )
        }

        "taskDetails" -> {

            TaskDetailsScreen(

                taskTitle = taskTitle,

                taskDescription = taskDescription,

                taskPriority = taskPriority,

                taskCategory = taskCategory,

                taskDueDate = taskDueDate,

                taskCompleted = taskCompleted,

                onEdit = {
                    currentScreen = "editTask"
                },

                onComplete = {

                    taskCompleted = true

                    scope.launch {

                        context.dataStore.edit { preferences ->
                            preferences[TASK_COMPLETED] = true
                        }
                    }

                    currentScreen = "dashboard"
                },

                onUndoComplete = {

                    taskCompleted = false

                    scope.launch {

                        context.dataStore.edit { preferences ->
                            preferences[TASK_COMPLETED] = false
                        }
                    }
                },

                onDelete = {

                    scope.launch {

                        context.dataStore.edit { preferences ->

                            preferences.remove(TASK_EXISTS)
                            preferences.remove(TASK_TITLE)
                            preferences.remove(TASK_DESCRIPTION)
                            preferences.remove(TASK_PRIORITY)
                            preferences.remove(TASK_CATEGORY)
                            preferences.remove(TASK_DUE_DATE)
                            preferences.remove(TASK_COMPLETED)
                        }
                    }

                    hasTask = false
                    taskTitle = ""
                    taskDescription = ""
                    taskPriority = "Medium"
                    taskCategory = "Study"
                    taskDueDate = "Today"
                    taskCompleted = false

                    currentScreen = "dashboard"
                },

                onBack = {
                    currentScreen = "dashboard"
                }
            )
        }

        "focus" -> {

            FocusScreen(

                focusDurationMinutes = focusDuration.toIntOrNull() ?: 25,

                onSessionComplete = {

                    focusSessions++
                    focusMinutes += focusDuration.toIntOrNull() ?: 25
                },

                onBack = {
                    currentScreen = "dashboard"
                }
            )
        }

        "settings" -> {

            SettingsScreen(
                context = context,
                userName = userName,
                notificationsEnabled = notificationsEnabled,
                focusDuration = focusDuration,
                onSaveName = { name ->
                    userName = name.ifBlank { "Focus User" }
                    scope.launch {
                        context.dataStore.edit { preferences ->
                            preferences[USER_NAME] = userName
                        }
                    }
                },
                onNotificationsChanged = { enabled ->
                    notificationsEnabled = enabled
                    scope.launch {
                        context.dataStore.edit { preferences ->
                            preferences[NOTIFICATIONS_ENABLED] = enabled
                        }
                    }
                    if (enabled) {
                        showNotification(context)
                    } else {
                        cancelNotification(context)
                    }
                },
                onFocusDurationChanged = { duration ->
                    focusDuration = duration
                    scope.launch {
                        context.dataStore.edit { preferences ->
                            preferences[FOCUS_DURATION] = duration
                        }
                    }
                },
                onClearData = {
                    scope.launch {
                        context.dataStore.edit { preferences ->
                            preferences.remove(TASK_EXISTS)
                            preferences.remove(TASK_TITLE)
                            preferences.remove(TASK_DESCRIPTION)
                            preferences.remove(TASK_PRIORITY)
                            preferences.remove(TASK_CATEGORY)
                            preferences.remove(TASK_DUE_DATE)
                            preferences.remove(TASK_COMPLETED)
                        }
                    }
                    hasTask = false
                    taskTitle = ""
                    taskDescription = ""
                    taskPriority = "Medium"
                    taskCategory = "Study"
                    taskDueDate = "Today"
                    taskCompleted = false
                },
                onBack = {
                    currentScreen = "dashboard"
                }
            )
        }

        "progress" -> {

            ProgressScreen(

                taskCompleted = taskCompleted,

                hasTask = hasTask,

                focusSessions = focusSessions,

                focusMinutes = focusMinutes,

                onBack = {
                    currentScreen = "dashboard"
                }
            )
        }
    }
}

fun showNotification(context: Context) {
    if (Build.VERSION.SDK_INT >= 33 &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) return

    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("FocusFlow")
        .setContentText("Stay focused and complete your tasks today!")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()

    context.getSystemService(NotificationManager::class.java)
        .notify(NOTIFICATION_ID, notification)
}

fun cancelNotification(context: Context) {
    context.getSystemService(NotificationManager::class.java)
        .cancel(NOTIFICATION_ID)
}

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit
) {

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF7F8FC)
    ) {

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),

            horizontalAlignment = Alignment.CenterHorizontally,

            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "FocusFlow",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF202124)
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = "Plan Better.\nFocus Better.\nAchieve More.",
                fontSize = 25.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = Color(0xFF424242)
            )

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            Text(
                text = "Organize your tasks, stay focused, and track your progress.",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF666666)
            )

            Spacer(
                modifier = Modifier.height(40.dp)
            )

            Button(

                onClick = onGetStarted,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),

                shape = RoundedCornerShape(16.dp)
            ) {

                Text(
                    text = "Get Started",
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(

    hasTask: Boolean,

    taskTitle: String,

    taskPriority: String,

    taskCategory: String,

    taskDueDate: String,

    taskCompleted: Boolean,

    userName: String,

    onAddTask: () -> Unit,

    onTaskDetails: () -> Unit,

    onFocus: () -> Unit,

    onProgress: () -> Unit,

    onSettings: () -> Unit
) {

    val currentHour =
        java.util.Calendar
            .getInstance()
            .get(java.util.Calendar.HOUR_OF_DAY)

    val greeting = when (currentHour) {

        in 5..11 -> "Good Morning ☀️"

        in 12..16 -> "Good Afternoon 🌤️"

        in 17..20 -> "Good Evening 🌆"

        else -> "Good Night 🌙"
    }

    val progress =
        if (hasTask) {

            if (taskCompleted)
                1f
            else
                0f

        } else {
            0f
        }

    val percentage =
        (progress * 100).toInt()

    Column(

        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 22.dp
            )
    ) {

        // HEADER

        Text(
            text = "$greeting, $userName",
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E1E1E)
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = "Let's make today productive.",
            fontSize = 15.sp,
            color = Color(0xFF707070)
        )

        Spacer(
            modifier = Modifier.height(22.dp)
        )


        // PROGRESS CARD

        Card(

            modifier = Modifier.fillMaxWidth(),

            shape = RoundedCornerShape(22.dp),

            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF202124)
            ),

            elevation = CardDefaults.cardElevation(
                defaultElevation = 5.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {

                        Text(
                            text = "Today's Progress",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(
                            modifier =
                                Modifier.height(5.dp)
                        )

                        Text(
                            text =
                                if (!hasTask)
                                    "Start by adding your first task."
                                else if (taskCompleted)
                                    "Excellent! Task completed."
                                else
                                    "Keep going. You've got this!",
                            fontSize = 14.sp,
                            color = Color(0xFFCCCCCC)
                        )
                    }

                    Text(
                        text = "$percentage%",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                LinearProgressIndicator(

                    progress = {
                        progress
                    },

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(8.dp),

                    color = Color.White,

                    trackColor =
                        Color(0xFF555555)
                )

                Spacer(
                    modifier =
                        Modifier.height(15.dp)
                )

                OutlinedButton(

                    onClick = onProgress,

                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(12.dp),

                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                ) {

                    Text(
                        text =
                            "View Detailed Progress"
                    )
                }
            }
        }


        Spacer(
            modifier =
                Modifier.height(25.dp)
        )


        // TASK HEADER

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(

                text = "Today's Tasks",

                modifier =
                    Modifier.weight(1f),

                fontSize = 21.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    Color(0xFF202124)
            )

            if (hasTask) {

                Text(
                    text =
                        if (taskCompleted)
                            "1 Completed"
                        else
                            "1 Pending",

                    fontSize = 13.sp,

                    color =
                        Color(0xFF777777)
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        if (hasTask) {

            Card(

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(20.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color.White
                    ),

                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 3.dp
                    )
            ) {

                Column(

                    modifier =
                        Modifier.padding(18.dp)
                ) {

                    Row(

                        modifier =
                            Modifier.fillMaxWidth(),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        // STATUS ICON

                        Surface(

                            modifier =
                                Modifier.width(46.dp)
                                    .height(46.dp),

                            shape =
                                RoundedCornerShape(14.dp),

                            color =
                                if (taskCompleted)
                                    Color(0xFFE3F5E9)
                                else
                                    Color(0xFFE8ECFF)
                        ) {

                            Column(

                                horizontalAlignment =
                                    Alignment.CenterHorizontally,

                                verticalArrangement =
                                    Arrangement.Center
                            ) {

                                Text(

                                    text =
                                        if (taskCompleted)
                                            "✓"
                                        else
                                            "○",

                                    fontSize = 23.sp,

                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }


                        Spacer(
                            modifier =
                                Modifier.width(13.dp)
                        )


                        Column(

                            modifier =
                                Modifier.weight(1f)
                        ) {

                            Text(

                                text = taskTitle,

                                fontSize = 18.sp,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    Color(0xFF202124)
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )

                            Row {

                                SmallInfoChip(
                                    text =
                                        taskCategory
                                )

                                Spacer(
                                    modifier =
                                        Modifier.width(7.dp)
                                )

                                SmallInfoChip(
                                    text =
                                        taskPriority
                                )
                            }
                        }
                    }


                    Spacer(
                        modifier =
                            Modifier.height(14.dp)
                    )


                    Row(

                        modifier =
                            Modifier.fillMaxWidth(),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(

                            text =
                                "📅 $taskDueDate",

                            fontSize = 13.sp,

                            color =
                                Color(0xFF707070),

                            modifier =
                                Modifier.weight(1f)
                        )

                        Text(

                            text =
                                if (taskCompleted)
                                    "Completed ✓"
                                else
                                    "Pending",

                            fontSize = 13.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                if (taskCompleted)
                                    Color(0xFF258A4A)
                                else
                                    Color(0xFF777777)
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.height(14.dp)
                    )


                    OutlinedButton(

                        onClick =
                            onTaskDetails,

                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(12.dp)
                    ) {

                        Text(
                            text =
                                "View Task Details →"
                        )
                    }
                }
            }

        } else {

            Card(

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(20.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color.White
                    )
            ) {

                Column(

                    modifier =
                        Modifier.padding(25.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "📝",
                        fontSize = 35.sp
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "No tasks added yet.",
                        fontSize = 18.sp,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(5.dp)
                    )

                    Text(
                        text =
                            "Create your first task and start focusing.",
                        fontSize = 14.sp,
                        color =
                            Color(0xFF777777),
                        textAlign =
                            TextAlign.Center
                    )
                }
            }
        }


        Spacer(
            modifier =
                Modifier.height(20.dp)
        )


        // ACTION BUTTONS

        Row(

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Button(

                onClick =
                    onAddTask,

                modifier =
                    Modifier
                        .weight(1f)
                        .height(52.dp),

                shape =
                    RoundedCornerShape(15.dp)
            ) {

                Text(
                    text =
                        "+ Add Task"
                )
            }

            Spacer(
                modifier =
                    Modifier.width(10.dp)
            )

            Button(

                onClick =
                    onFocus,

                modifier =
                    Modifier
                        .weight(1f)
                        .height(52.dp),

                shape =
                    RoundedCornerShape(15.dp),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF202124)
                    )
            ) {

                Text(
                    text =
                        "▶ Focus"
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        OutlinedButton(

            onClick =
                onProgress,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(50.dp),

            shape =
                RoundedCornerShape(15.dp)
        ) {

            Text(
                text =
                    "📊 My Progress"
            )
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        OutlinedButton(
            onClick = onSettings,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(15.dp)
        ) {
            Text(text = "⚙ Settings")
        }
    }
}


// =====================================================
// SMALL INFO CHIP
// =====================================================

@Composable
fun SmallInfoChip(
    text: String
) {

    Surface(

        shape =
            RoundedCornerShape(8.dp),

        color =
            Color(0xFFF0F1F5)
    ) {

        Text(

            text = text,

            modifier =
                Modifier.padding(
                    horizontal = 9.dp,
                    vertical = 5.dp
                ),

            fontSize = 11.sp,

            fontWeight =
                FontWeight.Medium,

            color =
                Color(0xFF555555)
        )
    }
}


// =====================================================
// TASK FORM
// =====================================================

@Composable
fun TaskFormScreen(

    screenTitle: String,

    buttonText: String,

    initialTitle: String,

    initialDescription: String,

    initialPriority: String,

    initialCategory: String,

    initialDueDate: String,

    onSave:
        (String, String, String, String, String) -> Unit,

    onBack: () -> Unit
) {

    var title by remember {
        mutableStateOf(initialTitle)
    }

    var description by remember {
        mutableStateOf(initialDescription)
    }

    var priority by remember {
        mutableStateOf(initialPriority)
    }

    var category by remember {
        mutableStateOf(initialCategory)
    }

    var dueDate by remember {
        mutableStateOf(initialDueDate)
    }

    Column(

        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = screenTitle,
            fontSize = 30.sp,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        OutlinedTextField(

            value = title,

            onValueChange = {
                title = it
            },

            label = {
                Text("Task Title")
            },

            placeholder = {
                Text("Example: Complete CN Assignment")
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine = true
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        OutlinedTextField(

            value = description,

            onValueChange = {
                description = it
            },

            label = {
                Text("Description")
            },

            placeholder = {
                Text("Describe your task...")
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(130.dp)
        )

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        Text(
            text = "Priority",
            fontSize = 18.sp,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Row(
            modifier =
                Modifier.fillMaxWidth()
        ) {

            PriorityButton(
                "Low",
                priority == "Low",
                { priority = "Low" },
                Modifier.weight(1f)
            )

            Spacer(
                modifier =
                    Modifier.width(6.dp)
            )

            PriorityButton(
                "Medium",
                priority == "Medium",
                { priority = "Medium" },
                Modifier.weight(1f)
            )

            Spacer(
                modifier =
                    Modifier.width(6.dp)
            )

            PriorityButton(
                "High",
                priority == "High",
                { priority = "High" },
                Modifier.weight(1f)
            )
        }

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        Text(
            text = "Category",
            fontSize = 18.sp,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Row(
            modifier =
                Modifier.fillMaxWidth()
        ) {

            CategoryButton(
                "Study",
                category == "Study",
                { category = "Study" },
                Modifier.weight(1f)
            )

            Spacer(
                modifier =
                    Modifier.width(6.dp)
            )

            CategoryButton(
                "Work",
                category == "Work",
                { category = "Work" },
                Modifier.weight(1f)
            )
        }

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Row(
            modifier =
                Modifier.fillMaxWidth()
        ) {

            CategoryButton(
                "Personal",
                category == "Personal",
                { category = "Personal" },
                Modifier.weight(1f)
            )

            Spacer(
                modifier =
                    Modifier.width(6.dp)
            )

            CategoryButton(
                "Other",
                category == "Other",
                { category = "Other" },
                Modifier.weight(1f)
            )
        }

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        Text(
            text = "Due Date",
            fontSize = 18.sp,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Row(
            modifier =
                Modifier.fillMaxWidth()
        ) {

            DueDateButton(
                "Today",
                dueDate == "Today",
                { dueDate = "Today" },
                Modifier.weight(1f)
            )

            Spacer(
                modifier =
                    Modifier.width(8.dp)
            )

            DueDateButton(
                "Tomorrow",
                dueDate == "Tomorrow",
                { dueDate = "Tomorrow" },
                Modifier.weight(1f)
            )
        }

        Spacer(
            modifier =
                Modifier.height(28.dp)
        )

        Button(

            onClick = {

                if (title.isNotBlank()) {

                    onSave(
                        title,
                        description,
                        priority,
                        category,
                        dueDate
                    )
                }
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(52.dp),

            shape =
                RoundedCornerShape(15.dp)
        ) {

            Text(
                text = buttonText
            )
        }

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        OutlinedButton(

            onClick = onBack,

            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(15.dp)
        ) {

            Text(
                text =
                    "← Back"
            )
        }
    }
}


// =====================================================
// PRIORITY BUTTON
// =====================================================

@Composable
fun PriorityButton(

    text: String,

    selected: Boolean,

    onClick: () -> Unit,

    modifier: Modifier
) {

    if (selected) {

        Button(
            onClick = onClick,
            modifier = modifier
        ) {

            Text(text)
        }

    } else {

        OutlinedButton(
            onClick = onClick,
            modifier = modifier
        ) {

            Text(text)
        }
    }
}


// =====================================================
// CATEGORY BUTTON
// =====================================================

@Composable
fun CategoryButton(

    text: String,

    selected: Boolean,

    onClick: () -> Unit,

    modifier: Modifier
) {

    if (selected) {

        Button(
            onClick = onClick,
            modifier = modifier
        ) {

            Text(text)
        }

    } else {

        OutlinedButton(
            onClick = onClick,
            modifier = modifier
        ) {

            Text(text)
        }
    }
}


// =====================================================
// DUE DATE BUTTON
// =====================================================

@Composable
fun DueDateButton(

    text: String,

    selected: Boolean,

    onClick: () -> Unit,

    modifier: Modifier
) {

    if (selected) {

        Button(
            onClick = onClick,
            modifier = modifier
        ) {

            Text(text)
        }

    } else {

        OutlinedButton(
            onClick = onClick,
            modifier = modifier
        ) {

            Text(text)
        }
    }
}


// =====================================================
// TASK DETAILS
// =====================================================

@Composable
fun TaskDetailsScreen(

    taskTitle: String,

    taskDescription: String,

    taskPriority: String,

    taskCategory: String,

    taskDueDate: String,

    taskCompleted: Boolean,

    onEdit: () -> Unit,

    onComplete: () -> Unit,

    onUndoComplete: () -> Unit,

    onDelete: () -> Unit,

    onBack: () -> Unit
) {

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp)
    ) {

        Text(
            text = "Task Details",
            fontSize = 30.sp,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(25.dp)
        )

        Card(

            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(20.dp),

            colors =
                CardDefaults.cardColors(
                    containerColor =
                        Color.White
                )
        ) {

            Column(
                modifier =
                    Modifier.padding(20.dp)
            ) {

                Text(
                    text = taskTitle,
                    fontSize = 24.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(18.dp)
                )

                Text(
                    text =
                        if (taskDescription.isBlank())
                            "No description added."
                        else
                            taskDescription,
                    fontSize = 16.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                Text(
                    text =
                        "Priority: $taskPriority",
                    fontSize = 16.sp,
                    fontWeight =
                        FontWeight.Medium
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Text(
                    text =
                        "Category: $taskCategory",
                    fontSize = 16.sp,
                    fontWeight =
                        FontWeight.Medium
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Text(
                    text =
                        "Due Date: $taskDueDate",
                    fontSize = 16.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(15.dp)
                )

                Text(
                    text =
                        if (taskCompleted)
                            "Status: Completed ✓"
                        else
                            "Status: Pending",
                    fontSize = 16.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(22.dp)
        )

        OutlinedButton(

            onClick = onEdit,

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                text =
                    "✏ Edit Task"
            )
        }

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        if (!taskCompleted) {

            Button(

                onClick = onComplete,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    text =
                        "✓ Mark as Complete"
                )
            }

        } else {

            Button(

                onClick =
                    onUndoComplete,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    text =
                        "↩ Mark as Pending"
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        OutlinedButton(

            onClick = {
                showDeleteDialog = true
            },

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                text =
                    "🗑 Delete Task"
            )
        }

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        OutlinedButton(

            onClick = onBack,

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                text =
                    "← Back to Dashboard"
            )
        }
    }

    if (showDeleteDialog) {

        AlertDialog(

            onDismissRequest = {
                showDeleteDialog = false
            },

            title = {
                Text(
                    text =
                        "Delete Task?"
                )
            },

            text = {
                Text(
                    text =
                        "Are you sure you want to permanently delete this task?"
                )
            },

            confirmButton = {

                Button(

                    onClick = {

                        showDeleteDialog = false
                        onDelete()
                    }
                ) {

                    Text(
                        text =
                            "Delete"
                    )
                }
            },

            dismissButton = {

                OutlinedButton(

                    onClick = {
                        showDeleteDialog = false
                    }
                ) {

                    Text(
                        text =
                            "Cancel"
                    )
                }
            }
        )
    }
}


// =====================================================
// FOCUS TIMER
// =====================================================

@Composable
fun FocusScreen(

    focusDurationMinutes: Int,

    onSessionComplete: () -> Unit,

    onBack: () -> Unit
) {

    var timeLeft by remember {
        mutableIntStateOf(focusDurationMinutes * 60)
    }

    var isRunning by remember {
        mutableStateOf(false)
    }

    var sessionRecorded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(isRunning) {

        while (
            isRunning &&
            timeLeft > 0
        ) {

            delay(1000)

            timeLeft--
        }

        if (
            timeLeft == 0 &&
            !sessionRecorded
        ) {

            isRunning = false
            sessionRecorded = true

            onSessionComplete()
        }
    }

    val minutes =
        timeLeft / 60

    val seconds =
        timeLeft % 60

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text =
                "Focus Session",
            fontSize = 30.sp,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        Text(
            text =
                "Focus on one task without distractions."
        )

        Spacer(
            modifier =
                Modifier.height(40.dp)
        )

        Text(

            text =
                String.format(
                    "%02d:%02d",
                    minutes,
                    seconds
                ),

            fontSize = 64.sp,

            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(32.dp)
        )

        Button(

            onClick = {
                isRunning = !isRunning
            }
        ) {

            Text(
                text =
                    if (isRunning)
                        "Pause"
                    else
                        "Start Timer"
            )
        }

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        OutlinedButton(

            onClick = {

                isRunning = false
                timeLeft = focusDurationMinutes * 60
                sessionRecorded = false
            }
        ) {

            Text(
                text =
                    "Reset"
            )
        }

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        OutlinedButton(
            onClick = onBack
        ) {

            Text(
                text =
                    "← Back to Dashboard"
            )
        }
    }
}


// =====================================================
// SETTINGS SCREEN
// =====================================================

@Composable
fun SettingsScreen(
    context: Context,
    userName: String,
    notificationsEnabled: Boolean,
    focusDuration: String,
    onSaveName: (String) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    onFocusDurationChanged: (String) -> Unit,
    onClearData: () -> Unit,
    onBack: () -> Unit
) {

    var name by remember(userName) { mutableStateOf(userName) }
    var showClearDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onNotificationsChanged(true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {

        Text(
            text = "Settings",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF202124)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Customize your FocusFlow experience.",
            fontSize = 15.sp,
            color = Color(0xFF707070)
        )

        Spacer(modifier = Modifier.height(22.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Profile",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { onSaveName(name) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Name")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Preferences",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notifications",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Enable productivity reminders",
                            fontSize = 13.sp,
                            color = Color(0xFF777777)
                        )
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled &&
                                Build.VERSION.SDK_INT >= 33 &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                notificationPermissionLauncher.launch(
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                            } else {
                                onNotificationsChanged(enabled)
                            }
                        }
                    )
                }

                if (notificationsEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showNotification(context) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🔔 Send Test Notification")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider()
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Default Focus Duration",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("15", "25", "45").forEachIndexed { index, duration ->
                        if (index > 0) Spacer(modifier = Modifier.width(6.dp))
                        if (focusDuration == duration) {
                            Button(
                                onClick = { onFocusDurationChanged(duration) },
                                modifier = Modifier.weight(1f)
                            ) { Text("${duration} min") }
                        } else {
                            OutlinedButton(
                                onClick = { onFocusDurationChanged(duration) },
                                modifier = Modifier.weight(1f)
                            ) { Text("${duration} min") }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Data",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🗑 Clear Task Data")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "About FocusFlow",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A simple productivity app for managing tasks, focusing with a timer, and tracking daily progress.",
                    fontSize = 14.sp,
                    color = Color(0xFF707070)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Version 1.0",
                    fontSize = 13.sp,
                    color = Color(0xFF888888)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(15.dp)
        ) {
            Text("← Back to Dashboard")
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Task Data?") },
            text = { Text("This will remove your current task and its saved details. Your settings will remain saved.") },
            confirmButton = {
                Button(onClick = {
                    showClearDialog = false
                    onClearData()
                }) {
                    Text("Clear Data")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


// =====================================================
// PROGRESS SCREEN
// =====================================================

@Composable
fun ProgressScreen(

    taskCompleted: Boolean,

    hasTask: Boolean,

    focusSessions: Int,

    focusMinutes: Int,

    onBack: () -> Unit
) {

    val completedTasks =
        if (taskCompleted) 1 else 0

    val totalTasks =
        if (hasTask) 1 else 0

    val progress =
        if (totalTasks > 0)

            completedTasks.toFloat() /
                    totalTasks.toFloat()

        else
            0f

    val percentage =
        (progress * 100).toInt()

    val hours =
        focusMinutes / 60

    val minutes =
        focusMinutes % 60

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp)
    ) {

        Text(
            text =
                "My Progress",
            fontSize = 32.sp,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(28.dp)
        )

        Card(

            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(20.dp)
        ) {

            Column(
                modifier =
                    Modifier.padding(20.dp)
            ) {

                Text(
                    text =
                        "Task Completion",
                    fontSize = 20.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Text(
                    text =
                        "$percentage%",
                    fontSize = 48.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                LinearProgressIndicator(

                    progress = {
                        progress
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                Text(
                    text =
                        "$completedTasks of $totalTasks tasks completed"
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(18.dp)
        )

        Card(

            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(20.dp)
        ) {

            Column(
                modifier =
                    Modifier.padding(20.dp)
            ) {

                Text(
                    text =
                        "Focus Statistics",
                    fontSize = 20.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                Text(
                    text =
                        "Focus Sessions: $focusSessions",
                    fontSize = 17.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                Text(
                    text =
                        "Total Focus Time: ${hours}h ${minutes}m",
                    fontSize = 17.sp
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(30.dp)
        )

        OutlinedButton(

            onClick = onBack,

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                text =
                    "← Back to Dashboard"
            )
        }
    }
}
