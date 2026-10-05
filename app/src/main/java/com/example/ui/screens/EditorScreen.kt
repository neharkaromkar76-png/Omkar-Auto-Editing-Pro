package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.KeyframeInspector
import com.example.ui.components.StageProgressModal
import com.example.ui.components.TimelineTrackView
import com.example.ui.components.VideoPreviewCanvas
import com.example.ui.theme.Cinematic3DIconContainer
import com.example.ui.theme.CinematicPillButton
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkVoid
import com.example.ui.theme.DeepCanvas
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleGlow
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
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

  // Hardware back press handler
  BackHandler(enabled = activeTab != EditorTab.PROJECT) {
    viewModel.setTab(EditorTab.PROJECT)
  }

  // 12-Stage Auto-Edit Progress Modal
  StageProgressModal(
    state = autoEditState,
    onDismiss = {}
  )

  Scaffold(
    modifier = modifier.fillMaxSize().background(DarkVoid),
    topBar = {
      // 1. TOP HEADER (3D CUTSZOOM AI Logo + Subtitle + Glowing Pill CTA)
      Surface(
        color = DarkVoid,
        modifier = Modifier.fillMaxWidth().statusBarsPadding()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Left: 3D Logo + Brand + Tagline
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Cinematic3DIconContainer(
              icon = Icons.Default.Videocam,
              contentDescription = "CutsZoom Logo",
              size = 38.dp,
              iconSize = 20.dp,
              gradientColors = listOf(NeonPurple, ElectricBlue)
            )

            Column(verticalArrangement = Arrangement.Center) {
              Text(
                text = "CUTSZOOM AI",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
              )
              Text(
                text = "Create. Edit. Automate.",
                color = Slate400,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
              )
            }
          }

          // Right: Large glowing pill/button: ✨ AUTO EDIT
          CinematicPillButton(
            text = "✨ AUTO EDIT",
            onClick = { viewModel.runAutoEdit() },
            modifier = Modifier.testTag("top_auto_edit_btn")
          )
        }
      }
    },
    bottomBar = {
      // FLOATING PREMIUM GLASS NAVIGATION BAR
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
          .padding(horizontal = 14.dp, vertical = 10.dp)
          .testTag("bottom_nav_bar"),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(
              elevation = 16.dp,
              shape = RoundedCornerShape(26.dp),
              spotColor = NeonPurple.copy(alpha = 0.35f),
              ambientColor = Color.Black
            )
            .clip(RoundedCornerShape(26.dp))
            .background(GlassSurface)
            .border(
              width = 1.dp,
              brush = Brush.horizontalGradient(
                listOf(
                  ElectricBlueGlow.copy(alpha = 0.4f),
                  NeonPurpleGlow.copy(alpha = 0.4f),
                  ElectricBlueGlow.copy(alpha = 0.3f)
                )
              ),
              shape = RoundedCornerShape(26.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
          ) {
            CinematicNavItem(
              label = "Project",
              icon = Icons.Default.Folder,
              isSelected = activeTab == EditorTab.PROJECT,
              onClick = { viewModel.setTab(EditorTab.PROJECT) }
            )
            CinematicNavItem(
              label = "Edit & Play",
              icon = Icons.Default.Timeline,
              isSelected = activeTab == EditorTab.TIMELINE,
              onClick = { viewModel.setTab(EditorTab.TIMELINE) }
            )
            CinematicNavItem(
              label = "Inspector",
              icon = Icons.Default.Tune,
              isSelected = activeTab == EditorTab.INSPECTOR,
              onClick = { viewModel.setTab(EditorTab.INSPECTOR) }
            )
            CinematicNavItem(
              label = "AI Scoring",
              icon = Icons.Default.Psychology,
              isSelected = activeTab == EditorTab.AI_ANALYSIS,
              onClick = { viewModel.setTab(EditorTab.AI_ANALYSIS) }
            )
            CinematicNavItem(
              label = "Export",
              icon = Icons.Default.Download,
              isSelected = activeTab == EditorTab.EXPORT,
              onClick = { viewModel.setTab(EditorTab.EXPORT) }
            )
          }
        }
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(DarkVoid)
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
            onUpdateStyleProfile = { p -> viewModel.updateStyleProfile(p) },
            onNavigateToAiScoring = { viewModel.setTab(EditorTab.AI_ANALYSIS) }
          )
        }

        EditorTab.TIMELINE -> {
          Column(modifier = Modifier.fillMaxSize()) {
            // Main Studio Center: Large Video Preview Area
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
                .padding(horizontal = 14.dp, vertical = 6.dp)
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
            VideoPreviewCanvas(
              metadata = metadata,
              currentScale = currentScale,
              currentTimeMs = currentTimeMs,
              currentFrame = currentFrame,
              isPlaying = isPlaying,
              onTogglePlayPause = { viewModel.togglePlayPause() },
              modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .padding(horizontal = 14.dp, vertical = 6.dp)
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
private fun CinematicNavItem(
  label: String,
  icon: ImageVector,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val iconColor by animateColorAsState(
    targetValue = if (isSelected) Color.White else Slate500,
    label = "nav_icon_color"
  )

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(20.dp))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
      )
      .then(
        if (isSelected) {
          Modifier
            .background(
              Brush.horizontalGradient(
                listOf(NeonPurple.copy(alpha = 0.5f), ElectricBlue.copy(alpha = 0.5f))
              )
            )
            .border(
              1.dp,
              Brush.horizontalGradient(listOf(NeonPurpleGlow, ElectricBlueGlow)),
              RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
        } else {
          Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        }
      ),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = iconColor,
        modifier = Modifier.size(18.dp)
      )
      if (isSelected) {
        Text(
          text = label,
          color = Color.White,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}
