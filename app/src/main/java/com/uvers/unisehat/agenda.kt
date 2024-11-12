package com.uvers.unisehat

import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.uvers.unisehat.control.LoadingScreen
import com.uvers.unisehat.control.getSavedCredentials
import com.uvers.unisehat.models.AgendaItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun Agenda() {
    val AgendaNavController = rememberNavController()

    NavHost(navController = AgendaNavController, startDestination = "agendaList") {
        composable("agendaList") {
            AgendaScreen(AgendaNavController)
        }
        composable("addAgenda") {
            AddAgendaScreen(AgendaNavController)
        }
        composable(
            "edit/{agendaId}",
            arguments = listOf(navArgument("agendaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val agendaId = backStackEntry.arguments?.getString("agendaId")
            agendaId?.let {
                EditAgendaScreen(AgendaNavController, agendaId)
            }
        }
        composable(
            "detail/{agendaId}",
            arguments = listOf(navArgument("agendaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val agendaId = backStackEntry.arguments?.getString("agendaId")
            agendaId?.let {
                DetailAgendaScreen(AgendaNavController, agendaId)
            }
        }
    }
}

@Composable
fun AgendaScreen(navController: NavController) {
    var selectedAgenda by remember { mutableStateOf<AgendaItem?>(null) }
    val scope = rememberCoroutineScope()
    val currentDate = Calendar.getInstance().time
    val agendaList = remember { mutableStateListOf<AgendaItem>() }
    var userRole by remember { mutableStateOf("") }

    val context = LocalContext.current
    val (identifier, role) = getSavedCredentials(context) ?: return

    val colors = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        fetchAgendaList { agendas ->
            agendaList.clear()
            agendaList.addAll(agendas)
        }
    }

    val upcomingAgenda = agendaList.filter { it.date >= currentDate }.sortedBy { it.date }
    val pastAgenda = agendaList.filter { it.date < currentDate }.sortedByDescending { it.date }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (role == "admin" || role == "dosen") {
            item {
                Row( modifier = Modifier
                    .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ){
                    Button(onClick = { navController.navigate("addAgenda") }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Add Agenda")
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            Text(
                text = "Upcoming Agenda",
                fontSize = 20.sp,
                color = colors.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (upcomingAgenda.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surface,
                    )
                ) {
                    Text(
                        text = "No Upcoming Agenda",
                        fontSize = 16.sp,
                        color = colors.onSurface,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(upcomingAgenda) { agenda ->
                AgendaCard(
                    agenda = agenda,
                    onDetailClick = { navController.navigate("detail/${agenda.identifier}") },
                    onEditClick = { navController.navigate("edit/${agenda.identifier}") },
                    onDeleteClick = { selectedAgenda = agenda },
                    userRole = userRole
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Past Agenda",
                fontSize = 20.sp,
                color = colors.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (pastAgenda.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceVariant,
                    )
                ) {
                    Text(
                        text = "No Past Agenda",
                        fontSize = 16.sp,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(pastAgenda) { agenda ->
                AgendaCard(
                    agenda = agenda,
                    onDetailClick = { navController.navigate("detail/${agenda.identifier}") },
                    onEditClick = { navController.navigate("edit/${agenda.identifier}") },
                    onDeleteClick = { selectedAgenda = agenda },
                    isPast = true,
                    userRole = userRole
                )
            }
        }


    }

    selectedAgenda?.let { agenda ->
        DetailDialog(
            agenda = agenda,
            onDismiss = { selectedAgenda = null },
            onDeleteClick = {
                scope.launch {
                    deleteAgenda(agenda) {
                        agendaList.remove(agenda)
                        selectedAgenda = null
                    }
                }
            }
        )
    }
}

@Composable
fun AgendaCard(
    agenda: AgendaItem,
    onDetailClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    userRole: String,
    isPast: Boolean = false
) {
    val context = LocalContext.current
    val (identifier, role) = getSavedCredentials(context) ?: return

    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val formattedDate = dateFormatter.format(agenda.date)

    val containerColor = if (isPast) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(onClick = onDetailClick),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(Color.Transparent),
        ) {
            Text(text = agenda.title, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurface)
            Text(text = "Date: $formattedDate", fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp),  color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(modifier = Modifier.padding(top = 8.dp)) {
                IconButton(onClick = onDetailClick) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Detail")
                }
                if (role == "admin" || role == "dosen"){
                IconButton(onClick = onEditClick) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
                }
            }
        }
    }
}

@Composable
fun DetailDialog(agenda: AgendaItem, onDismiss: () -> Unit, onDeleteClick: () -> Unit) {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val formattedDate = dateFormatter.format(agenda.date)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Confirm Delete ")
        },
        text = {
            Text("Are you sure you want to delete Agenda ${agenda.title}?")
        },
        confirmButton = {
            Button(onClick = onDeleteClick, colors = ButtonDefaults.buttonColors(Color.Red)) {
                Text("Delete")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun AddAgendaScreen(navController: NavController) {
    var title by remember { mutableStateOf(TextFieldValue()) }
    var description by remember { mutableStateOf(TextFieldValue()) }
    var date by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var titleError by remember { mutableStateOf(false) }
    var descriptionError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            isError = titleError,
            singleLine = true,
        )
        if (titleError) {
            Text(text = "Title cannot be empty", color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            isError = descriptionError
        )
        if (descriptionError) {
            Text(text = "Description cannot be empty", color = Color.Red, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = date?.let { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it) } ?: "",
            onValueChange = {},
            label = { Text("Date") },
            isError = dateError,
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        showDatePicker = true
                    })
                },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Pick Day")
                }
            },
            readOnly = true
        )
        if (dateError) {
            Text(text = "Date cannot be empty", color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row{
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(Color.Red)
            ) {
                Text("Back")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                titleError = title.text.isEmpty()
                descriptionError = description.text.isEmpty()
                dateError = date == null

                if (!titleError && !descriptionError && !dateError){
                    val agenda = AgendaItem(
                        identifier = UUID.randomUUID().toString(),
                        title = title.text,
                        date = date ?: Calendar.getInstance().time,
                        description = description.text
                    )

                    isLoading = true
                    addAgenda(agenda) {
                        isLoading = false
                        navController.popBackStack()
                    }
                }
            }) {
                Text("Add Agenda")
            }
        }

    }

    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Select Date") },
            text = {
                AndroidView(
                    factory = { context ->
                        CalendarView(context).apply {
                            setOnDateChangeListener { _, year, month, dayOfMonth ->
                                val calendar = Calendar.getInstance().apply {
                                    set(year, month, dayOfMonth)
                                }
                                date = calendar.time
                                showDatePicker = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(300.dp)
                )
            },
            confirmButton = {
                Button(onClick = { showDatePicker = false }) {
                    Text("Close")
                }
            }
        )
    }

    LoadingScreen(isLoading)
}

@Composable
fun EditAgendaScreen(navController: NavController, agendaId: String) {
    var agenda by remember { mutableStateOf<AgendaItem?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var titleError by remember { mutableStateOf(false) }
    var descriptionError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }


    LaunchedEffect(agendaId) {
        fetchAgenda(agendaId) { fetchedAgenda ->
            agenda = fetchedAgenda
        }
    }

    agenda?.let { currentAgenda ->
        var title by remember { mutableStateOf(TextFieldValue(currentAgenda.title)) }
        var description by remember { mutableStateOf(TextFieldValue(currentAgenda.description)) }
        var date by remember { mutableStateOf(currentAgenda.date) }
        var showDatePicker by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                isError = titleError,
                singleLine = true,
            )
            if (titleError) {
                Text(text = "Title cannot be empty", color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                isError = descriptionError
            )
            if (descriptionError) {
                Text(text = "Description cannot be empty", color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = date?.let { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it) } ?: "",
                onValueChange = {},
                label = { Text("Date") },
                isError = dateError,
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            showDatePicker = true
                        })
                    },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Pick Day")
                    }
                }
            )
            if (dateError) {
                Text(text = "Date cannot be empty", color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row{
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(Color.Red)
                ) {
                    Text("Back")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = {
                    titleError = title.text.isEmpty()
                    descriptionError = description.text.isEmpty()
                    dateError = date == null

                    if (!titleError && !descriptionError && !dateError){
                        isLoading = true
                        val updatedAgenda = currentAgenda.copy(
                            title = title.text,
                            date = date,
                            description = description.text
                        )
                        updateAgenda(updatedAgenda) {
                            isLoading = false
                            navController.popBackStack()
                        }
                    }

                }) {
                    Text("Update Agenda")
                }
            }

        }

        if (showDatePicker) {
            AlertDialog(
                onDismissRequest = { showDatePicker = false },
                title = { Text("Select Date") },
                text = {
                    AndroidView(
                        factory = { context ->
                            CalendarView(context).apply {
                                setOnDateChangeListener { _, year, month, dayOfMonth ->
                                    val calendar = Calendar.getInstance().apply {
                                        set(year, month, dayOfMonth)
                                    }
                                    date = calendar.time
                                    showDatePicker = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(300.dp)
                    )
                },
                confirmButton = {
                    Button(onClick = { showDatePicker = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
    LoadingScreen(isLoading)
}
@Composable
fun DetailAgendaScreen(navController: NavController, agendaId: String) {
    var agenda by remember { mutableStateOf<AgendaItem?>(null) }

    LaunchedEffect(agendaId) {
        fetchAgenda(agendaId) { fetchedAgenda ->
            agenda = fetchedAgenda
        }
    }

    val colors = MaterialTheme.colorScheme

    agenda?.let { currentAgenda ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentAgenda.title,
                        fontSize = 24.sp,
                        color = colors.primary,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = currentAgenda.description,
                        fontSize = 16.sp,
                        color = colors.onSurface,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val formattedDate = dateFormatter.format(currentAgenda.date)
                    Text(
                        text = "Date: $formattedDate",
                        fontSize = 16.sp,
                        color = colors.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = colors.onSurface.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceVariant)
                        ) {
                            Text("Back", color = colors.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    } ?: run {
        // Show a loading indicator or placeholder if the agenda is not yet loaded
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = colors.primary)
        }
    }
}


fun fetchAgendaList(onComplete: (List<AgendaItem>) -> Unit) {
    val database = FirebaseDatabase.getInstance().reference.child("agenda")

    database.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val agendaList = mutableListOf<AgendaItem>()
            snapshot.children.forEach { childSnapshot ->
                val agenda = childSnapshot.getValue(AgendaItem::class.java)
                agenda?.let { agendaList.add(it) }
            }
            onComplete(agendaList)
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle error
        }
    })
}

fun fetchAgenda(agendaId: String, onComplete: (AgendaItem?) -> Unit) {
    val database = FirebaseDatabase.getInstance().reference.child("agenda").child(agendaId)

    database.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val agenda = snapshot.getValue(AgendaItem::class.java)
            onComplete(agenda)
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle error
        }
    })
}

fun addAgenda(agenda: AgendaItem, onComplete: (Boolean) -> Unit) {
    val database = FirebaseDatabase.getInstance().reference.child("agenda").child(agenda.identifier)

    database.setValue(agenda)
        .addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
        .addOnFailureListener {
            // Handle failure
        }
}

fun updateAgenda(agenda: AgendaItem, onComplete: (Boolean) -> Unit) {
    val database = FirebaseDatabase.getInstance().reference.child("agenda").child(agenda.identifier)

    database.setValue(agenda)
        .addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
        .addOnFailureListener {
            // Handle failure
        }
}

fun deleteAgenda(agenda: AgendaItem, onComplete: (Boolean) -> Unit) {
    val database = FirebaseDatabase.getInstance().reference.child("agenda").child(agenda.identifier)

    database.removeValue()
        .addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
        .addOnFailureListener {
            // Handle failure
        }
}

