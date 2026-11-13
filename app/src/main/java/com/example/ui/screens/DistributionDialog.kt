package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppDistributionConfig
import com.example.data.seed.RyujinNovelMeta

@Composable
fun AppDistributionDialog(
    config: AppDistributionConfig,
    isExclusiveCreator: Boolean,
    onSaveConfig: (sharedPreviewUrl: String, apkDownloadUrl: String, versionLabel: String, releaseNotes: String) -> Unit,
    onDownloadBundleToUri: (Uri) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var previewUrl by remember(config.sharedPreviewUrl) { mutableStateOf(config.sharedPreviewUrl) }
    var apkUrl by remember(config.apkDownloadUrl) { mutableStateOf(config.apkDownloadUrl) }
    var versionLabel by remember(config.versionLabel) { mutableStateOf(config.versionLabel) }
    var releaseNotes by remember(config.releaseNotes) { mutableStateOf(config.releaseNotes) }

    val bundleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) onDownloadBundleToUri(uri)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.InstallMobile, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Unduh & Bagikan Ryujin Studio")
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Auth & Role Summary Card
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Identitas Akses Terhubung",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "• Developer/Kreator/Author Eksklusif: ${RyujinNovelMeta.CREATOR_EMAIL} (terverifikasi otomatis via Google Sign-In).\n• Pembaca & Penulis Lain: masuk otomatis menggunakan akun Google (email masing-masing).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Shared Preview Link for Readers
                Text(
                    text = "Link Akses Preview Pembaca (${config.versionLabel})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                ) {
                    Text(
                        text = config.sharedPreviewUrl,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { copyTextToClipboard(context, "Link Ryujin Studio", config.sharedPreviewUrl) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_reader_link_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salin Link")
                    }
                    Button(
                        onClick = {
                            val shareText = buildString {
                                append("Baca & gabung di Ryujin Studio (900 Bab • 3 Arc • 400 Karakter)!\n")
                                append("Masuk dengan akun Google Anda melalui link resmi:\n")
                                append(config.sharedPreviewUrl)
                                if (config.apkDownloadUrl.isNotBlank()) {
                                    append("\n\nLink Unduh APK Android:\n")
                                    append(config.apkDownloadUrl)
                                }
                            }
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Undangan Membaca — Ryujin Studio")
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Bagikan Link Unduh Ryujin Studio"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_reader_link_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bagikan")
                    }
                }

                // Direct APK Download & Offline Package Button
                if (config.apkDownloadUrl.isNotBlank()) {
                    Button(
                        onClick = { openExternalUrl(context, config.apkDownloadUrl) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_apk_download_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unduh APK Resmi (${config.versionLabel})")
                    }
                }

                OutlinedButton(
                    onClick = { bundleLauncher.launch("Ryujin_Studio_Katalog_dan_Akses.txt") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("download_offline_bundle_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unduh Paket Info & Katalog Novel")
                }

                if (isExclusiveCreator) {
                    HorizontalDivider()
                    Text(
                        text = "Kontrol Developer: Atur Link Unduh APK",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hasilkan APK melalui menu Pengaturan (⚙️) AI Studio -> Unggah ke Google Drive/GitHub -> Tempel tautannya di bawah agar muncul untuk semua pembaca.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = versionLabel,
                        onValueChange = { versionLabel = it },
                        label = { Text("Label Versi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = apkUrl,
                        onValueChange = { apkUrl = it },
                        label = { Text("URL Unduh APK Resmi (Drive / Release)") },
                        placeholder = { Text("https://drive.google.com/...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("apk_url_input")
                    )
                    OutlinedTextField(
                        value = previewUrl,
                        onValueChange = { previewUrl = it },
                        label = { Text("URL Preview Bersama untuk Pembaca") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = releaseNotes,
                        onValueChange = { releaseNotes = it },
                        label = { Text("Catatan Rilis") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { onSaveConfig(previewUrl, apkUrl, versionLabel, releaseNotes) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_distribution_config_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan & Publikasikan Link Unduh")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

private fun copyTextToClipboard(context: Context, label: String, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    cm?.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "Link disalin ke clipboard!", Toast.LENGTH_SHORT).show()
}

private fun openExternalUrl(context: Context, url: String) {
    val normalized = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(normalized)))
    } catch (_: Exception) {
        Toast.makeText(context, "Tidak dapat membuka tautan unduhan.", Toast.LENGTH_SHORT).show()
    }
}
