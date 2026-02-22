package ru.profia.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ru.profia.app.R
import ru.profia.app.data.model.KS3Certificate
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.viewmodel.ProjectTitleViewModel
import ru.profia.app.ui.theme.Divider
import ru.profia.app.ui.theme.Primary
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KS3Screen(
    navController: NavController,
    projectId: String,
    projectTitleViewModel: ProjectTitleViewModel = hiltViewModel()
) {
    val projectName by projectTitleViewModel.projectName.collectAsState()
    var ks3Cert by remember {
        mutableStateOf(KS3Certificate(projectId = projectId))
    }
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    BaseScreen(
        navController = navController,
        title = projectName ?: stringResource(R.string.ks3_screen_title)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ks3_form_title),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.ks3_form_subtitle),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.ks2_doc_number, ks3Cert.number), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stringResource(R.string.ks2_date, dateFormat.format(ks3Cert.date)), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = stringResource(R.string.ks2_report_period, dateFormat.format(ks3Cert.reportPeriodStart), dateFormat.format(ks3Cert.reportPeriodEnd)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(stringResource(R.string.ks3_contract, ks3Cert.contractNumber), fontWeight = FontWeight.Medium)
                    Text(stringResource(R.string.ks3_contract_date, dateFormat.format(ks3Cert.contractDate)), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            val items = ks3Cert.items
            val totalForPeriod = items.sumOf { it.costForPeriod }
            val totalFromStart = items.sumOf { it.costFromStart }
            val vatAmount = totalForPeriod * ks3Cert.vatRate / 100
            val totalWithVat = totalForPeriod + vatAmount

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(R.string.ks3_column_name),
                            modifier = Modifier.weight(2f),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            stringResource(R.string.ks3_column_from_start),
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.End
                        )
                        Text(
                            stringResource(R.string.ks3_column_for_period),
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.End
                        )
                    }
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 8.dp))

                    if (items.isEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(R.string.ks3_default_work_type),
                                modifier = Modifier.weight(2f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "0",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.End
                            )
                            Text(
                                "0",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.End
                            )
                        }
                    } else {
                        items.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    row.name,
                                    modifier = Modifier.weight(2f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "%,.0f".format(row.costFromStart),
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.End
                                )
                                Text(
                                    "%,.0f".format(row.costForPeriod),
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.End
                                )
                            }
                            HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }

                    HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.ks2_total), modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                        Text(
                            "%,.0f".format(totalFromStart),
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End
                        )
                        Text(
                            "%,.0f".format(totalForPeriod),
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.ks2_vat, ks3Cert.vatRate.toInt()), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "%,.0f".format(vatAmount),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End
                        )
                    }
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.ks2_total_to_pay), fontWeight = FontWeight.Bold, color = Primary)
                        Text(
                            "%,.0f".format(totalWithVat),
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}
