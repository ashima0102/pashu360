# Reports Module
## Smart Dairy Farm Management System

---

## Report Types

| Report | Data | Format | Frequency |
|---|---|---|---|
| Daily Milk Production | Per-animal AM/PM + herd total | PDF / Share | Daily |
| Monthly Milk Summary | 30-day totals, avg, top producers | PDF / CSV | Monthly |
| Yearly Production | 12-month comparison | PDF / CSV | Yearly |
| Vaccination Report | Compliance %, schedule, overdue | PDF | On-demand |
| Health Summary | Disease frequency, treatments | PDF | On-demand |
| Breeding & Conception | Conception rates, services | PDF | On-demand |
| Financial P&L | Income, expenses, profit | PDF / CSV | Monthly |
| Animal History Report | Complete lifetime record | PDF | Per-animal |

---

## Reports Screen UI

```kotlin
@Composable
fun ReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Reports") }) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp)) {

            // Date Range Selector
            item {
                DateRangeSelector(
                    startDate = uiState.startDate,
                    endDate = uiState.endDate,
                    onRangeChanged = viewModel::onDateRangeChanged
                )
            }

            // Quick presets
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip("Today", uiState.preset == "today") { viewModel.setPreset("today") }
                    FilterChip("This Week", uiState.preset == "week") { viewModel.setPreset("week") }
                    FilterChip("This Month", uiState.preset == "month") { viewModel.setPreset("month") }
                    FilterChip("Custom", uiState.preset == "custom") { viewModel.setPreset("custom") }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { SectionHeader("Production Reports") }

            // Report cards
            item {
                ReportCard(
                    icon = Icons.Default.LocalDrink,
                    title = "Milk Production Report",
                    description = "Daily/monthly totals, per-animal breakdown",
                    onGenerate = { viewModel.generateMilkReport() },
                    isGenerating = uiState.generatingReport == "milk"
                )
            }
            item {
                ReportCard(
                    icon = Icons.Default.Vaccines,
                    title = "Vaccination Report",
                    description = "Compliance %, schedule, overdue animals",
                    onGenerate = { viewModel.generateVaccinationReport() },
                    isGenerating = uiState.generatingReport == "vaccination"
                )
            }
            item {
                ReportCard(
                    icon = Icons.Default.Favorite,
                    title = "Health Summary",
                    description = "Disease records, treatments, vet visits",
                    onGenerate = { viewModel.generateHealthReport() },
                    isGenerating = uiState.generatingReport == "health"
                )
            }
            item {
                ReportCard(
                    icon = Icons.Default.AccountBalance,
                    title = "Financial Report",
                    description = "Income, expenses, profit & loss",
                    onGenerate = { viewModel.generateFinancialReport() },
                    isGenerating = uiState.generatingReport == "financial"
                )
            }
        }
    }

    // Share generated PDF
    uiState.generatedPdfUri?.let { uri ->
        LaunchedEffect(uri) {
            sharePdf(context, uri, "Smart Dairy Report")
            viewModel.onReportShared()
        }
    }
}
```

---

## PDF Generation (iTextPDF)

```kotlin
// PdfExporter.kt
class PdfExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun generateMilkReport(
        farm: Farm,
        startDate: LocalDate,
        endDate: LocalDate,
        records: List<DailyMilkSummary>,
        animalBreakdown: List<AnimalMilkSummary>
    ): Uri {
        val fileName = "milk_report_${startDate}_${endDate}.pdf"
        val file = File(context.cacheDir, fileName)

        PdfWriter(file).use { writer ->
            val pdf = PdfDocument(writer)
            val document = Document(pdf, PageSize.A4)

            // ── HEADER ──────────────────────────────────────────────
            val headerFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
            val bodyFont = PdfFontFactory.createFont(StandardFonts.HELVETICA)
            val green = DeviceRgb(46, 125, 50)  // Brand green

            // Logo + Farm name
            document.add(
                Paragraph("SMART DAIRY FARM")
                    .setFont(headerFont)
                    .setFontSize(20f)
                    .setFontColor(green)
            )
            document.add(
                Paragraph(farm.name)
                    .setFont(headerFont)
                    .setFontSize(14f)
            )
            document.add(
                Paragraph("Milk Production Report")
                    .setFont(bodyFont)
                    .setFontSize(12f)
                    .setFontColor(DeviceGray.GRAY)
            )
            document.add(
                Paragraph("Period: $startDate to $endDate")
                    .setFont(bodyFont)
                    .setFontSize(10f)
            )
            document.add(HorizontalRule().setStrokeColor(green))

            // ── SUMMARY BOX ──────────────────────────────────────
            val totalProduction = records.sumOf { it.total }
            val dailyAverage = totalProduction / records.size
            val bestDay = records.maxByOrNull { it.total }

            val summaryTable = Table(floatArrayOf(1f, 1f, 1f)).useAllAvailableWidth()
            summaryTable.addSummaryCell("Total Production", "${totalProduction.roundTo2}L")
            summaryTable.addSummaryCell("Daily Average", "${dailyAverage.roundTo2}L")
            summaryTable.addSummaryCell("Best Day", "${bestDay?.date}: ${bestDay?.total?.roundTo2}L")
            document.add(summaryTable)
            document.add(Paragraph("\n"))

            // ── DAILY PRODUCTION TABLE ───────────────────────────
            document.add(
                Paragraph("Daily Production").setFont(headerFont).setFontSize(12f)
            )
            val dailyTable = Table(floatArrayOf(2f, 2f, 2f, 2f)).useAllAvailableWidth()
            dailyTable.addHeaderCell("Date")
            dailyTable.addHeaderCell("Morning (L)")
            dailyTable.addHeaderCell("Evening (L)")
            dailyTable.addHeaderCell("Total (L)")

            records.forEach { day ->
                dailyTable.addCell(day.date.toString())
                dailyTable.addCell(day.morningTotal.roundTo2.toString())
                dailyTable.addCell(day.eveningTotal.roundTo2.toString())
                dailyTable.addCell(day.total.roundTo2.toString())
            }
            document.add(dailyTable)
            document.add(Paragraph("\n"))

            // ── PER-ANIMAL BREAKDOWN ──────────────────────────
            document.add(
                Paragraph("Animal Breakdown").setFont(headerFont).setFontSize(12f)
            )
            val animalTable = Table(floatArrayOf(1f, 2f, 1f, 1f, 1f)).useAllAvailableWidth()
            animalTable.addHeaderCell("Tag")
            animalTable.addHeaderCell("Name")
            animalTable.addHeaderCell("Total (L)")
            animalTable.addHeaderCell("Daily Avg")
            animalTable.addHeaderCell("Peak")

            animalBreakdown.sortedByDescending { it.totalLitres }.forEach { animal ->
                animalTable.addCell(animal.tagId)
                animalTable.addCell(animal.name ?: "-")
                animalTable.addCell(animal.totalLitres.roundTo2.toString())
                animalTable.addCell(animal.dailyAverage.roundTo2.toString())
                animalTable.addCell(animal.peakDay.roundTo2.toString())
            }
            document.add(animalTable)

            // ── FOOTER ──────────────────────────────────────────
            document.add(HorizontalRule())
            document.add(
                Paragraph("Generated by Smart Dairy Farm Management System on ${LocalDate.now()}")
                    .setFont(bodyFont).setFontSize(8f)
                    .setFontColor(DeviceGray.GRAY)
            )

            document.close()
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    // Similar functions for:
    // generateVaccinationReport()
    // generateHealthReport()
    // generateFinancialReport()
    // generateAnimalHistoryReport()
}

private val Double.roundTo2: Double get() = (this * 100).toLong() / 100.0

private fun Table.addHeaderCell(text: String) {
    addHeaderCell(Cell().add(Paragraph(text).setBold().setFontSize(10f)))
}

private fun Table.addSummaryCell(label: String, value: String) {
    addCell(Cell().add(
        Paragraph("$label\n$value")
            .setBold().setFontSize(11f)
            .setTextAlignment(TextAlignment.CENTER)
    ).setPadding(8f))
}
```

---

## CSV Export

```kotlin
fun exportMilkReportAsCsv(
    records: List<DailyMilkSummary>,
    animalBreakdown: List<AnimalMilkSummary>
): Uri {
    val fileName = "milk_report.csv"
    val file = File(context.cacheDir, fileName)

    file.bufferedWriter().use { writer ->
        // Header row
        writer.write("Date,Morning (L),Evening (L),Total (L)\n")

        // Daily records
        records.forEach { day ->
            writer.write("${day.date},${day.morningTotal},${day.eveningTotal},${day.total}\n")
        }

        writer.write("\n")
        writer.write("Tag ID,Animal Name,Total (L),Daily Average,Peak Day\n")

        // Per-animal
        animalBreakdown.forEach { animal ->
            writer.write("${animal.tagId},${animal.name ?: ""},${animal.totalLitres},${animal.dailyAverage},${animal.peakDay}\n")
        }
    }

    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}
```

---

## Share Report

```kotlin
// Share via Android Share Sheet (WhatsApp, Email, Drive, etc.)
fun sharePdf(context: Context, uri: Uri, title: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, title)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Report via..."))
}

// Share CSV
fun shareCsv(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Export Report via..."))
}
```

---

## ReportsViewModel.kt

```kotlin
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val generateMilkReportUseCase: GenerateMilkReportUseCase,
    private val generateVaccinationReportUseCase: GenerateVaccinationReportUseCase,
    private val pdfExporter: PdfExporter,
    private val activeFarmPrefs: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    fun generateMilkReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(generatingReport = "milk") }
            try {
                val farmId = activeFarmPrefs.getActiveFarmId() ?: return@launch
                val data = generateMilkReportUseCase(
                    farmId, _uiState.value.startDate, _uiState.value.endDate
                )
                val uri = pdfExporter.generateMilkReport(
                    farm = data.farm,
                    startDate = _uiState.value.startDate,
                    endDate = _uiState.value.endDate,
                    records = data.dailySummaries,
                    animalBreakdown = data.animalBreakdown
                )
                _uiState.update { it.copy(generatingReport = null, generatedPdfUri = uri) }
            } catch (e: Exception) {
                _uiState.update { it.copy(generatingReport = null, error = e.message) }
            }
        }
    }

    fun onReportShared() {
        _uiState.update { it.copy(generatedPdfUri = null) }
    }
}
```
