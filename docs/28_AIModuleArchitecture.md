# AI Module Architecture (V2)
## Smart Dairy Farm Management System

---

## Overview

The AI Smart Assistant is a V2 feature scheduled for Month 7–10. It adds predictive and conversational intelligence on top of the farm's collected data.

```
AI Module Components
├── On-Device ML (TensorFlow Lite)
│   ├── Disease prediction from symptoms
│   ├── Heat detection from milk pattern
│   └── Image-based disease detection
│
├── Cloud AI (Claude API via Supabase Edge Function)
│   ├── Veterinary chatbot
│   ├── AI report generation
│   ├── Feed recommendation engine
│   └── Financial forecasting
│
└── Analytics Engine (PostgreSQL queries)
    ├── Milk yield prediction (curve fitting)
    ├── Conception rate trends
    └── Farm performance scoring
```

---

## AI Feature 1 — Disease Prediction (On-Device TF Lite)

### Model Input
```kotlin
data class DiseasePredictionInput(
    val symptoms: List<String>,       // ["fever", "diarrhea", "not_eating"]
    val temperature: Float,           // 40.8
    val bcs: Float,                   // 2.5
    val ageMonths: Int,               // 48
    val breed: String,                // "HF"
    val lactationStage: Int,          // day in milk
    val recentVaccinations: List<String>  // ["FMD", "BQ"]
)

data class DiseasePrediction(
    val disease: String,
    val confidence: Float,            // 0.0–1.0
    val urgency: String,              // "low", "medium", "high", "emergency"
    val suggestedActions: List<String>
)
```

### Implementation
```kotlin
// DiseasePredictionUseCase.kt
class DiseasePredictionUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val interpreter: Interpreter by lazy {
        val model = FileUtil.loadMappedFile(context, "disease_prediction.tflite")
        Interpreter(model)
    }

    suspend operator fun invoke(input: DiseasePredictionInput): List<DiseasePrediction> =
        withContext(Dispatchers.Default) {
            val inputTensor = preprocessInput(input)
            val outputTensor = Array(1) { FloatArray(NUM_DISEASES) }
            interpreter.run(inputTensor, outputTensor)
            postprocessOutput(outputTensor[0])
        }

    private fun preprocessInput(input: DiseasePredictionInput): Array<FloatArray> {
        // Encode symptoms as one-hot vector
        // Normalize continuous features
        // Return as float tensor
        val vector = FloatArray(INPUT_SIZE)
        SYMPTOM_VOCAB.forEachIndexed { i, symptom ->
            vector[i] = if (symptom in input.symptoms) 1f else 0f
        }
        vector[SYMPTOM_VOCAB.size] = input.temperature / 45f
        vector[SYMPTOM_VOCAB.size + 1] = input.bcs / 5f
        // ... other features
        return arrayOf(vector)
    }

    companion object {
        const val INPUT_SIZE = 64
        const val NUM_DISEASES = 20
        val SYMPTOM_VOCAB = listOf("fever", "diarrhea", "limping", "not_eating",
            "bloat", "cough", "nasal_discharge", "eye_discharge", "skin_lesion", ...)
    }
}
```

---

## AI Feature 2 — Veterinary Chatbot (Claude API)

```kotlin
// VetChatbotUseCase.kt — calls Supabase Edge Function which calls Claude API
class VetChatbotUseCase @Inject constructor(
    private val postgrest: Postgrest
) {
    suspend operator fun invoke(
        question: String,
        animalContext: Animal?,
        conversationHistory: List<ChatMessage>
    ): Result<String> = runCatching {
        // Call Supabase Edge Function (keeps Claude API key server-side)
        val response = postgrest.rpc(
            function = "vet_chatbot",
            body = mapOf(
                "question" to question,
                "animal_context" to animalContext?.let {
                    mapOf(
                        "breed" to it.breed,
                        "age_months" to it.ageMonths,
                        "gender" to it.gender.value,
                        "status" to it.status.value,
                        "recent_diseases" to it.recentDiseases?.map { d -> d.diagnosis }
                    )
                },
                "conversation_history" to conversationHistory.map {
                    mapOf("role" to it.role, "content" to it.content)
                }
            )
        )
        response.decodeAs<String>()
    }
}

// supabase/functions/vet-chatbot/index.ts
import Anthropic from "npm:@anthropic-ai/sdk"

serve(async (req) => {
  const { question, animal_context, conversation_history } = await req.json()

  const client = new Anthropic({ apiKey: Deno.env.get("CLAUDE_API_KEY") })

  const systemPrompt = `You are a veterinary assistant for dairy farmers in India.
You help farm owners understand animal health issues, suggest immediate actions, and advise when to call a vet.

${animal_context ? `Current animal context:
- Breed: ${animal_context.breed}
- Age: ${animal_context.age_months} months
- Status: ${animal_context.status}
- Recent diseases: ${animal_context.recent_diseases?.join(', ') || 'none'}` : ''}

IMPORTANT RULES:
1. Always recommend consulting a licensed veterinarian for diagnosis and treatment
2. Provide first-aid and immediate care advice only
3. Respond in simple, farmer-friendly language
4. If it sounds like an emergency, clearly state to call a vet IMMEDIATELY
5. Keep responses concise (under 200 words)`

  const messages = [
    ...conversation_history,
    { role: "user", content: question }
  ]

  const response = await client.messages.create({
    model: "claude-sonnet-4-6",
    max_tokens: 512,
    system: systemPrompt,
    messages
  })

  return new Response(
    JSON.stringify(response.content[0].text),
    { headers: { "Content-Type": "application/json" } }
  )
})
```

---

## AI Feature 3 — Heat Detection from Milk Drop

```kotlin
// HeatDetectionFromMilkUseCase.kt
class HeatDetectionFromMilkUseCase @Inject constructor(
    private val milkDao: MilkRecordDao
) {
    suspend operator fun invoke(animalId: String): HeatPrediction? {
        // Get last 30 days of milk records
        val records = milkDao.getLast30DaysForAnimal(animalId)
        if (records.size < 7) return null

        // Calculate 7-day rolling average
        val rollingAvg = records.windowed(7).map { window ->
            window.sumOf { it.quantityLiters } / 7.0
        }

        // Detect significant drop (>15% below 7-day average)
        val currentDayAvg = records.takeLast(2).sumOf { it.quantityLiters }
        val previousAvg = rollingAvg.dropLast(1).average()

        val dropPercent = ((previousAvg - currentDayAvg) / previousAvg) * 100

        return if (dropPercent >= 15) {
            HeatPrediction(
                animalId = animalId,
                confidence = (dropPercent / 30.0).coerceAtMost(1.0).toFloat(),
                dropPercent = dropPercent,
                message = "Milk dropped ${dropPercent.roundTo1}% — check for heat signs"
            )
        } else null
    }
}

data class HeatPrediction(
    val animalId: String,
    val confidence: Float,
    val dropPercent: Double,
    val message: String
)
```

---

## AI Feature 4 — Image-Based Disease Detection

```kotlin
// ImageDiseaseDetectionUseCase.kt
class ImageDiseaseDetectionUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val interpreter: Interpreter by lazy {
        val model = FileUtil.loadMappedFile(context, "disease_image_model.tflite")
        Interpreter(model)
    }

    suspend operator fun invoke(bitmap: Bitmap): ImageDiseaseResult =
        withContext(Dispatchers.Default) {
            // Resize to model input (224x224)
            val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

            // Normalize pixels to [-1, 1]
            val inputBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
            resized.forEachPixel { _, _, pixel ->
                inputBuffer.putFloat(((pixel.red - 128) / 128f))
                inputBuffer.putFloat(((pixel.green - 128) / 128f))
                inputBuffer.putFloat(((pixel.blue - 128) / 128f))
            }

            val output = Array(1) { FloatArray(DISEASE_CLASSES.size) }
            interpreter.run(inputBuffer, output)

            val predictions = output[0].mapIndexed { i, conf ->
                ImageDiseasePrediction(DISEASE_CLASSES[i], conf)
            }.sortedByDescending { it.confidence }.take(3)

            ImageDiseaseResult(predictions)
        }

    companion object {
        val DISEASE_CLASSES = listOf(
            "FMD_lesion", "ringworm", "tick_infestation",
            "mange", "lumpy_skin", "pink_eye", "warts",
            "abscess", "wound", "normal"
        )
    }
}
```

---

## AI Feature 5 — Farm Performance Score

```kotlin
// FarmPerformanceScoreUseCase.kt
class FarmPerformanceScoreUseCase @Inject constructor(
    private val milkDao: MilkRecordDao,
    private val vaccinationDao: VaccinationDao,
    private val healthDao: HealthCheckupDao,
    private val breedingDao: BreedingRecordDao
) {
    suspend operator fun invoke(farmId: String): FarmScore {
        // Score 0–100 based on weighted KPIs

        // 1. Vaccination compliance (30 points)
        val vaccinationScore = calculateVaccinationScore(farmId)

        // 2. Milk production vs breed average (30 points)
        val milkScore = calculateMilkScore(farmId)

        // 3. Average BCS (20 points)
        val healthScore = calculateHealthScore(farmId)

        // 4. Conception rate (20 points)
        val breedingScore = calculateBreedingScore(farmId)

        val totalScore = (vaccinationScore * 0.3 +
                         milkScore * 0.3 +
                         healthScore * 0.2 +
                         breedingScore * 0.2).toInt()

        return FarmScore(
            total = totalScore,
            grade = when {
                totalScore >= 90 -> "A+"
                totalScore >= 80 -> "A"
                totalScore >= 70 -> "B"
                totalScore >= 60 -> "C"
                else -> "D"
            },
            breakdown = ScoreBreakdown(
                vaccination = vaccinationScore.toInt(),
                milk = milkScore.toInt(),
                health = healthScore.toInt(),
                breeding = breedingScore.toInt()
            ),
            insights = generateInsights(vaccinationScore, milkScore, healthScore, breedingScore)
        )
    }
}
```

---

## AI Chat UI

```kotlin
@Composable
fun AiChatScreen(
    animalId: String? = null,
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Vet Assistant") },
                actions = { Icon(Icons.Default.SmartToy, null) }
            )
        },
        bottomBar = {
            Row(modifier = Modifier.padding(16.dp).imePadding()) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask about your animal...") },
                    modifier = Modifier.weight(1f),
                    maxLines = 3
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    },
                    enabled = inputText.isNotBlank() && !uiState.isTyping
                ) {
                    Icon(Icons.Default.Send, "Send")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            reverseLayout = true,
            contentPadding = PaddingValues(16.dp)
        ) {
            if (uiState.isTyping) {
                item { TypingIndicator() }
            }
            items(uiState.messages.reversed()) { message ->
                ChatBubble(message = message)
            }
        }
    }
}
```

---

## Model Files (TF Lite)

```
android/app/src/main/assets/
├── disease_prediction.tflite    # ~2MB — symptom → disease
├── disease_image_model.tflite   # ~10MB — image → disease class
└── heat_detection.tflite        # ~1MB — milk pattern → heat probability

Training data sources:
- Disease prediction: Anonymized symptom/diagnosis records from all opted-in farms
- Image model: Transfer learning on MobileNetV3 with vetted disease images
- Update frequency: Monthly model refresh via Play Store update
```

---

## AI Module Rollout Plan

| Month | Feature |
|---|---|
| Month 7 | Milk-based heat detection (rule-based first, then ML) |
| Month 7 | Veterinary chatbot (Claude API via Edge Function) |
| Month 8 | Disease prediction (TF Lite v1 — 10 diseases) |
| Month 9 | Farm Performance Score |
| Month 9 | AI-generated monthly report |
| Month 10 | Image disease detection (TF Lite) |
| Month 11 | Animal face recognition (research) |
| Month 12 | Financial forecasting |
