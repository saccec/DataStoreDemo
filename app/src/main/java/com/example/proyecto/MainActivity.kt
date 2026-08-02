// package com.example.proyecto
package com.example.proyecto
import com.example.proyecto.ui.theme.ProyectoTheme
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// ----------------------------------------------------
// 1. CONFIGURACIÓN DEL SISTEMA DE DATASTORE
// ----------------------------------------------------

val Context.datastore by preferencesDataStore(name = "academic_settings")

// ----------------------------------------------------
// 2. CLASE DE PERSISTENCIA MÍNIMA (DataStoreMin)
// ----------------------------------------------------

class DataStoreMin(private val context: Context) {

    // CREATE / UPDATE: Guarda un par clave-valor de forma asíncrona
    suspend fun save(id: String, category: String) {
        val key = stringPreferencesKey(id)
        context.datastore.edit { preferences ->
            preferences[key] = category
        }
    }

    // READ: Lee el valor asociado de forma asíncrona
    suspend fun getCategory(id: String): String? {
        val key = stringPreferencesKey(id)

        return context.datastore.data
            .map { preferences -> preferences[key] }
            .first()
    }
}

// ----------------------------------------------------
// 3. ACTIVIDAD PRINCIPAL (MainActivity)
// ----------------------------------------------------

class MainActivity : ComponentActivity() {

    private val TAG = "DataStoreTest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val storage = DataStoreMin(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DataStoreScreen(storage = storage, tag = TAG)
                }
            }
        }
    }
}

// ----------------------------------------------------
// 4. INTERFAZ GRÁFICA CON JETPACK COMPOSE
// ----------------------------------------------------

@Composable
fun DataStoreScreen(storage: DataStoreMin, tag: String) {

    val coroutineScope = rememberCoroutineScope()

    var documentId by remember { mutableStateOf("1") }
    var categoryValue by remember { mutableStateOf("IX Tableau") }
    var resultadoLectura by remember { mutableStateOf("Presiona guardar o leer") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Persistencia local con DataStore",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Resultado: $resultadoLectura",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Botón Guardar (CREATE / UPDATE)
        Button(
            onClick = {
                coroutineScope.launch {

                    Log.d(tag, "Iniciando guardado...")

                    storage.save(documentId, categoryValue)

                    resultadoLectura = "Guardado: $categoryValue"

                    Log.d(
                        tag,
                        "Guardado exitoso ID: $documentId asignado a '$categoryValue'"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Guardar Dato")
        }

        // Botón Leer (READ)
        Button(
            onClick = {
                coroutineScope.launch {

                    Log.d(tag, "Iniciando lectura...")

                    val recuperado = storage.getCategory(documentId)

                    resultadoLectura = recuperado ?: "No encontrado"

                    Log.d(tag, "Lectura exitosa: $recuperado")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Leer Dato Persistido")
        }
    }
}
