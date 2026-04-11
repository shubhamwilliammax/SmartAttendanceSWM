package com.swm.smartattendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swm.smartattendance.model.ShortForm
import com.swm.smartattendance.model.ShortFormType
import com.swm.smartattendance.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortFormManagerScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val isEnabled by viewModel.isShortFormEnabled.collectAsState()
    val classes by viewModel.classes.collectAsState()
    val selectedClassId by viewModel.selectedClassId.collectAsState()
    val shortForms by viewModel.shortForms.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingShortForm by remember { mutableStateOf<ShortForm?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF00C853)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("S", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Short Form Settings", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Custom")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Enable Short Forms Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Enable Short Forms",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Applies to Branch Names and Subject Names",
                                        color = Color.Gray,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { viewModel.toggleShortForm(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF00C853),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color(0xFF333333)
                            )
                        )
                    }
                }
            }

            item {
                Text(
                    "Select Class to View Short Forms",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                var expanded by remember { mutableStateOf(false) }
                Box {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E1E1E)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = classes.find { it.id == selectedClassId }?.name ?: "All Classes",
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Classes") },
                            onClick = {
                                viewModel.selectClass(null)
                                expanded = false
                            }
                        )
                        classes.forEach { cls ->
                            DropdownMenuItem(
                                text = { Text(cls.name) },
                                onClick = {
                                    viewModel.selectClass(cls.id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "AVAILABLE SHORT FORMS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(shortForms) { sf ->
                ShortFormItem(sf) {
                    editingShortForm = sf
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showAddDialog) {
        AddEditShortFormDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { fullName, shortForm, type ->
                viewModel.addShortForm(fullName, shortForm, type)
                showAddDialog = false
            }
        )
    }

    if (editingShortForm != null) {
        AddEditShortFormDialog(
            shortForm = editingShortForm,
            onDismiss = { editingShortForm = null },
            onConfirm = { fullName, shortForm, type ->
                viewModel.updateShortForm(editingShortForm!!.copy(fullName = fullName, shortForm = shortForm, type = type))
                editingShortForm = null
            }
        )
    }
}

@Composable
fun ShortFormItem(sf: ShortForm, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sf.fullName,
                color = Color.White,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp
            )
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF2C2C2C),
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = sf.shortForm,
                    color = Color.LightGray,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AddEditShortFormDialog(
    shortForm: ShortForm? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, ShortFormType) -> Unit
) {
    var fullName by remember { mutableStateOf(shortForm?.fullName ?: "") }
    var shortName by remember { mutableStateOf(shortForm?.shortForm ?: "") }
    var type by remember { mutableStateOf(shortForm?.type ?: ShortFormType.BRANCH) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (shortForm == null) "Add Short Form" else "Edit Short Form") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = shortName,
                    onValueChange = { shortName = it },
                    label = { Text("Short Form") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = type == ShortFormType.BRANCH, onClick = { type = ShortFormType.BRANCH })
                    Text("Branch")
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = type == ShortFormType.SUBJECT, onClick = { type = ShortFormType.SUBJECT })
                    Text("Subject")
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (fullName.isNotBlank() && shortName.isNotBlank()) onConfirm(fullName, shortName, type) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
