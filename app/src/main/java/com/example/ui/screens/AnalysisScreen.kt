package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditTimeline
import com.example.model.SpeechBoundary
import com.example.model.SpeechWord
import com.example.ui.theme.AmberZoom
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepSlate950
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldCut
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.SurfaceCard

@Composable
fun AnalysisScreen(
  timeline: EditTimeline?,
  onSelectBoundary: (SpeechBoundary) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedSubTab by remember { mutableStateOf(0) }
  val subTabs = listOf("Boundaries & Scoring", "Word Timestamps", "Canonical Timeline JSON")

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DeepSlate950)
  ) {
    ScrollableTabRow(
      selectedTabIndex = selectedSubTab,
      containerColor = DeepSlate950,
      contentColor = ElectricIndigo,
      edgePadding = 12.dp
    ) {
      subTabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedSubTab == index,
          onClick = { selectedSubTab = index },
          text = {
            Text(
              text = title,
              fontSize = 12.sp,
              fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal,
              color = if (selectedSubTab == index) CyberCyan else Slate400
            )
          }
        )
      }
    }

    if (timeline == null) {
      Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Text("No analysis available yet. Run 'AUTO EDIT' to generate.", color = Slate500, fontSize = 14.sp)
      }
      return
    }

    when (selectedSubTab) {
      0 -> BoundariesList(timeline.boundaries, onSelectBoundary)
      1 -> WordTimestampsList(timeline.segments.flatMap { it.words })
      2 -> TimelineJsonView(timeline)
    }
  }
}

@Composable
private fun BoundariesList(
  boundaries: List<SpeechBoundary>,
  onSelectBoundary: (SpeechBoundary) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(12.dp).testTag("boundaries_list"),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    items(boundaries) { b ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onSelectBoundary(b) },
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = AmberZoom
              ) {
                Text(
                  text = "SPLIT @ ${String.format("%.3f", b.time)}s",
                  color = Color.Black,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
              Text("Frame ${b.frame}", color = Slate400, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }

            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (b.boundaryConfidence >= 0.85) EmeraldCut else AmberZoom
            ) {
              Text(
                text = "${(b.boundaryConfidence * 100).toInt()}% CONFIDENCE",
                color = Color.Black,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Text(
            text = "Reason: ${b.reason}",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )

          // 5-Metric Breakdown
          val s = b.scoreDetails
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            ScoreBadge("Semantic", s.semanticCompletionScore, EmeraldCut)
            ScoreBadge("Punctuation", s.punctuationScore, CyberCyan)
            ScoreBadge("Pause", s.pauseScore, AmberZoom)
            ScoreBadge("Rhythm", s.rhythmScore, ElectricIndigo)
          }
        }
      }
    }
  }
}

@Composable
private fun ScoreBadge(label: String, score: Double, color: Color) {
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(Slate850)
      .padding(horizontal = 6.dp, vertical = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(label, color = Slate500, fontSize = 9.sp)
    Text("${(score * 100).toInt()}%", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
  }
}

@Composable
private fun WordTimestampsList(words: List<SpeechWord>) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(12.dp).testTag("words_list"),
    verticalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    items(words) { word ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(SurfaceCard)
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = word.word,
          color = Color.White,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "[${String.format("%.2f", word.startTime)}s - ${String.format("%.2f", word.endTime)}s]",
            color = CyberCyan,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "${(word.confidence * 100).toInt()}%",
            color = EmeraldCut,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@Composable
private fun TimelineJsonView(timeline: EditTimeline) {
  val canonicalJson = buildString {
    appendLine("{")
    appendLine("  \"source\": {")
    appendLine("    \"duration\": ${String.format("%.3f", timeline.source.durationSeconds)},")
    appendLine("    \"fps\": ${String.format("%.5f", timeline.source.fps)},")
    appendLine("    \"width\": ${timeline.source.width},")
    appendLine("    \"height\": ${timeline.source.height}")
    appendLine("  },")
    appendLine("  \"boundaries\": [")
    timeline.boundaries.forEachIndexed { idx, b ->
      val comma = if (idx < timeline.boundaries.size - 1) "," else ""
      appendLine("    { \"time\": ${String.format("%.3f", b.time)}, \"frame\": ${b.frame}, \"confidence\": ${String.format("%.2f", b.boundaryConfidence)} }$comma")
    }
    appendLine("  ],")
    appendLine("  \"zoomEvents\": [")
    timeline.zoomEvents.forEachIndexed { idx, e ->
      val comma = if (idx < timeline.zoomEvents.size - 1) "," else ""
      appendLine("    {")
      appendLine("      \"boundaryFrame\": ${e.boundaryFrame},")
      appendLine("      \"interpolation\": \"${e.interpolation}\",")
      appendLine("      \"keyframes\": [")
      e.keyframes.forEachIndexed { kidx, k ->
        val kcomma = if (kidx < e.keyframes.size - 1) "," else ""
        appendLine("        { \"frame\": ${k.frame}, \"scale\": ${String.format("%.2f", k.scale)} }$kcomma")
      }
      appendLine("      ]")
      appendLine("    }$comma")
    }
    appendLine("  ]")
    appendLine("}")
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(12.dp)
  ) {
    Card(
      modifier = Modifier.fillMaxSize(),
      colors = CardDefaults.cardColors(containerColor = Slate850),
      shape = RoundedCornerShape(12.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
    ) {
      LazyColumn(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        item {
          Text(
            text = canonicalJson,
            color = CyberCyan,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 16.sp
          )
        }
      }
    }
  }
}
