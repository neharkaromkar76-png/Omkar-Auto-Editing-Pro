package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpeechBoundary
import com.example.model.ZoomEvent
import com.example.ui.components.KeyframeInspector
import com.example.ui.components.StageProgressModal
import com.example.ui.components.TimelineTrackView
import com.example.ui.components.VideoPreviewCanvas
import com.example.ui.theme.AmberZoom
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepSlate950
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldCut
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.viewmodel.EditorTab
import com.example.viewmodel.EditorViewModel

@Composable
fun EditorScreen(
  viewModel: EditorViewModel,
  modifier: Modifier = Modifier
) {
  val activeTab by viewModel.activeTab.collectAsState()
  val metadata by viewModel.mediaMetadata.collectAsState()
  val timeline by viewModel.timeline.collectAsState()
  val waveform by viewModel.waveform.collectAsState()
  val autoEditState by viewModel.autoEditState.collectAsState()
  val exportSettings by viewModel.exportSettings.collectAsState()
  val exportResult by viewModel.exportResult.collectAsState()
  val isPlaying by viewModel.isPlaying.collectAsState()
  val currentTimeMs by viewModel.currentTimeMs.collectAsState()
  val currentFrame by viewModel.currentFrame.collectAsState()
  val currentScale by viewModel.currentScale.collectAsState()
  val selectedZoomEvent by viewModel.selectedZoomEvent.collectAsState()
  val styleProfile by viewModel.styleProfile.collectAsState()
  val customApiKey by viewModel.customApiKey.collectAsState()

  // Handle hardware back press gracefully
  BackHandler(enabled = activeTab != EditorTab.TIMELINE) {
    viewModel.setTab(EditorTab.TIMELINE)
  }

  // 12-Stage Auto-Edit Progress Modal
  StageProgressModal(
    state = autoEditState,
    onDismiss = {}
  )

  Scaffold(
    modifier = modifier.fillMaxSize().background(DeepSlate950),
    topBar = {
      // Top Navigation / Mode Bar
      Surface(
        color = DeepSlate950,
        modifier = Modifier.fillMaxWidth().statusBarsPadding()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              shape = CircleShape,
              color = AmberZoom,
              modifier = Modifier.size(26.dp)
            ) {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.padding(4.dp))
            }
            Text("CUTSZOOM AI", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }

          // Primary Quick Action: [ AUTO EDIT ]
          Button(
            onClick = { viewModel.runAutoEdit() },
            colors = ButtonDefaults.buttonColors(
              containerColor = ElectricIndigo,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.testTag("top_auto_edit_btn")
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AmberZoom, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
            Text("AUTO EDIT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    },
    bottomBar = {
      // Bottom Navigation Bar
      NavigationBar(
        containerColor = DeepSlate950,
        contentColor = Slate400,
        modifier = Modifier.navigationBarsPadding().testTag("bottom_nav_bar")
      ) {
        NavigationBarItem(
          selected = activeTab == EditorTab.PROJECT,
          onClick = { viewModel.setTab(EditorTab.PROJECT) },
          icon = { Icon(Icons.Default.Folder, contentDescription = "Project") },
          label = { Text("Project", fontSize = 10.sp) },
          colors = navItemColors()
        )
        NavigationBarItem(
          selected = activeTab == EditorTab.TIMELINE,
          onClick = { viewModel.setTab(EditorTab.TIMELINE) },
          icon = { Icon(Icons.Default.Timeline, contentDescription = "Timeline") },
          label = { Text("Edit & Play", fontSize = 10.sp) },
          colors = navItemColors()
        )
        NavigationBarItem(
          selected = activeTab == EditorTab.INSPECTOR,
          onClick = { viewModel.setTab(EditorTab.INSPECTOR) },
          icon = { Icon(Icons.Default.Tune, contentDescription = "Inspector") },
          label = { Text("Inspector", fontSize = 10.sp) },
          colors = navItemColors()
        )
        NavigationBarItem(
          selected = activeTab == EditorTab.AI_ANALYSIS,
          onClick = { viewModel.setTab(EditorTab.AI_ANALYSIS) },
          icon = { Icon(Icons.Default.Psychology, contentDescription = "AI Analysis") },
          label = { Text("AI Scoring", fontSize = 10.sp) },
          colors = navItemColors()
        )
        NavigationBarItem(
          selected = activeTab == EditorTab.EXPORT,
          onClick = { viewModel.setTab(EditorTab.EXPORT) },
          icon = { Icon(Icons.Default.Download, contentDescription = "Export") },
          label = { Text("Export", fontSize = 10.sp) },
          colors = navItemColors()
        )
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(DeepSlate950)
    ) {
      when (activeTab) {
        EditorTab.PROJECT -> {
          ProjectScreen(
            metadata = metadata,
            styleProfile = styleProfile,
            customApiKey = customApiKey,
            onPickUserVideo = { uri -> viewModel.loadUserVideo(uri) },
            onLoadSampleVideo = { viewModel.loadSampleVideo() },
            onRunAutoEdit = { viewModel.runAutoEdit() },
            onSetCustomApiKey = { key -> viewModel.setCustomApiKey(key) },
            onUpdateStyleProfile = { p -> viewModel.updateStyleProfile(p) }
          )
        }

        EditorTab.TIMELINE -> {
          Column(modifier = Modifier.fillMaxSize()) {
            // Main Studio Center: Large Video Preview
            VideoPreviewCanvas(
              metadata = metadata,
              currentScale = currentScale,
              currentTimeMs = currentTimeMs,
              currentFrame = currentFrame,
              isPlaying = isPlaying,
              onTogglePlayPause = { viewModel.togglePlayPause() },
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 6.dp)
            )

            // Bottom Multi-Track Professional Timeline
            TimelineTrackView(
              timeline = timeline,
              waveform = waveform,
              currentTimeMs = currentTimeMs,
              currentFrame = currentFrame,
              isPlaying = isPlaying,
              selectedZoomEvent = selectedZoomEvent,
              onTogglePlayPause = { viewModel.togglePlayPause() },
              onSeekTo = { ms -> viewModel.seekTo(ms) },
              onSelectZoomEvent = { event ->
                viewModel.selectZoomEvent(event)
                viewModel.setTab(EditorTab.INSPECTOR)
              },
              onAddSplitAtPlayhead = { viewModel.addSplitAtCurrentPlayhead() },
              onDeleteSelectedSplit = { viewModel.deleteSelectedZoomEvent() }
            )
          }
        }

        EditorTab.INSPECTOR -> {
          val assocBoundary = timeline?.boundaries?.find {
            it.frame == selectedZoomEvent?.boundaryFrame
          }

          Column(modifier = Modifier.fillMaxSize()) {
            // Mini preview header to observe zoom effect adjustments live
            VideoPreviewCanvas(
              metadata = metadata,
              currentScale = currentScale,
              currentTimeMs = currentTimeMs,
              currentFrame = currentFrame,
              isPlaying = isPlaying,
              onTogglePlayPause = { viewModel.togglePlayPause() },
              modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(8.dp)
            )

            KeyframeInspector(
              selectedZoomEvent = selectedZoomEvent,
              associatedBoundary = assocBoundary,
              onUpdateScale = { s -> viewModel.updateSelectedZoomScale(s) },
              onUpdateDuration = { d -> viewModel.updateSelectedZoomDuration(d) },
              onUpdatePosition = { x, y -> viewModel.updateSelectedPosition(x, y) },
              onDeleteEvent = {
                viewModel.deleteSelectedZoomEvent()
                viewModel.setTab(EditorTab.TIMELINE)
              },
              modifier = Modifier.weight(1f)
            )
          }
        }

        EditorTab.AI_ANALYSIS -> {
          AnalysisScreen(
            timeline = timeline,
            onSelectBoundary = { b ->
              val event = timeline?.zoomEvents?.find { it.boundaryFrame == b.frame }
              viewModel.selectZoomEvent(event)
              viewModel.setTab(EditorTab.TIMELINE)
            }
          )
        }

        EditorTab.EXPORT -> {
          ExportScreen(
            timeline = timeline,
            settings = exportSettings,
            autoEditState = autoEditState,
            exportResult = exportResult,
            onUpdateSettings = { s -> viewModel.updateExportSettings(s) },
            onStartExport = { viewModel.exportVideo() }
          )
        }
      }
    }
  }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
  selectedIconColor = Color.White,
  selectedTextColor = Color.White,
  indicatorColor = ElectricIndigo,
  unselectedIconColor = Slate500,
  unselectedTextColor = Slate500
)
