package com.example.typingtest

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.typingtest.ui.theme.TypingTestTheme
import kotlinx.coroutines.delay
import org.xmlpull.v1.XmlPullParser

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TypingTestTheme {
                TypingScreen()
            }
        }
    }
}
fun parse(context: Context): List<String> {
    val words = mutableListOf<String>()
    val parser = context.resources.getXml(R.xml.words)
    var event = parser.eventType
    while (event != XmlPullParser.END_DOCUMENT){
        if(event == XmlPullParser.START_TAG && parser.name == "word"){
            parser.next()
            words.add(parser.text)
        }
        event = parser.next()
    }
    return words
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypingScreen() {
    val context = LocalContext.current
    val allWords = remember { parse(context) }
    val displayed = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        displayed.addAll(allWords.shuffled().take(5))
    }

    var typedText by remember { mutableStateOf(TextFieldValue("")) }
    var typedCount by remember { mutableStateOf(0) }
    val startTime = remember { System.currentTimeMillis() }
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    var lastTypedTime by remember { mutableStateOf(System.currentTimeMillis()) }

    //display WPM
    val elapsedMinutes = (currentTime - startTime) / 60000f
    val wpm = if (elapsedMinutes > 0) typedCount / elapsedMinutes else 0f

    // shuffles the words every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            val idleTime = System.currentTimeMillis() - lastTypedTime
            if (typedText.text.isEmpty() && idleTime > 3000 && displayed.isNotEmpty()) {
                //shuffle
                for (i in displayed.indices) {
                    displayed[i] = allWords.random()
                }
            }
        }
    }
    fun checkMatch(newValue: String) {
        if (newValue.isBlank()) return
        val typed = newValue.trim()
        val matchIndex = displayed.indexOf(typed)
        if (matchIndex >= 0) {
            displayed.removeAt(matchIndex)
            typedCount++
            displayed.add(allWords.random())

            // *** Important: clear the typed text ***
            typedText = TextFieldValue("")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Typing Speed: %.1f WPM".format(wpm)) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = typedText,
                onValueChange = {
                    typedText = it
                    lastTypedTime = System.currentTimeMillis()  // <-- update last-typed time
                    checkMatch(typedText.text)
                },
                label = { Text("Type here") }
            )


            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                items(displayed) { word ->
                    Text(word, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TypingTestTheme {
        TypingScreen()
    }
}
