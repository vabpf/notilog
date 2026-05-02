package com.notilog.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(systemId: Int, tag: String?) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Detail for ID: $systemId")
            Text("Tag: $tag")
        }
    }
}
