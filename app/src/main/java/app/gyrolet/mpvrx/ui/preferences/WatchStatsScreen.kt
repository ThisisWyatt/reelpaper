@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package app.gyrolet.mpvrx.ui.preferences

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import app.gyrolet.mpvrx.presentation.components.AppAlertDialog as AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import app.gyrolet.mpvrx.presentation.components.AppDialogTextButton as TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.gyrolet.mpvrx.R
import app.gyrolet.mpvrx.repository.WatchStatsRepository
import app.gyrolet.mpvrx.repository.WatchStatsSnapshot
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons
import app.gyrolet.mpvrx.ui.theme.paper.LocalPaperColors
import app.gyrolet.mpvrx.ui.theme.paper.paperInset
import app.gyrolet.mpvrx.ui.theme.paper.paperRaise
import app.gyrolet.mpvrx.ui.theme.AppMotion
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileWatchStatistics() {
  val configuration = androidx.compose.ui.platform.LocalConfiguration.current
  val isTablet = configuration.smallestScreenWidthDp >= 600
  val repository = koinInject<WatchStatsRepository>()
  val scope = rememberCoroutineScope()
  val revision by repository.revision.collectAsStateWithLifecycle()
  val stats by produceState(WatchStatsSnapshot(), repository, revision) { value = repository.snapshot() }
  var confirmReset by rememberSaveable { mutableStateOf(false) }
  val paperColors = LocalPaperColors.current

  Column(
    modifier = Modifier.fillMaxWidth().padding(horizontal = if (isTablet) 24.dp else 20.dp),
  ) {
    // .sec：字距标题 + 右侧隐私说明。
    Row(
      modifier = Modifier.fillMaxWidth().padding(start = 2.dp, end = 2.dp, top = 20.dp, bottom = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = stringResource(R.string.watch_stats_title),
        modifier = Modifier.alignByBaseline().semantics { heading() },
        fontSize = 12.5.sp,
        letterSpacing = 3.sp,
        fontWeight = FontWeight.ExtraBold,
        color = paperColors.inkTertiary,
      )
      Text(
        text = stringResource(R.string.profile_privacy_note),
        modifier = Modifier.alignByBaseline(),
        fontSize = 11.5.sp,
        letterSpacing = 1.sp,
        color = paperColors.inkSecondary,
      )
    }

    // .statcard：总览双数字 / 墨条分类 / 朱砂墨锭柱，一张纸卡。
    val statShape = RoundedCornerShape(14.dp)
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .paperRaise(statShape, paperColors)
          .border(1.dp, paperColors.cardLine, statShape),
    ) {
      // .strow2: two big figures with a hairline between.
      Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(vertical = 15.dp),
      ) {
        StatCell(
          value = (((stats.totalSeconds + 59) / 60).coerceAtLeast(0)).toString(),
          unit = stringResource(R.string.profile_unit_minutes),
          label = stringResource(R.string.watch_stats_total_time),
          modifier = Modifier.weight(1f),
        )
        Box(
          Modifier
            .width(1.dp)
            .fillMaxHeight()
            .padding(vertical = 3.dp)
            .background(paperColors.line),
        )
        StatCell(
          value = stats.sessions.toString(),
          unit = stringResource(R.string.profile_unit_times),
          label = stringResource(R.string.watch_stats_total_sessions),
          modifier = Modifier.weight(1f),
        )
      }

      // .statsec: 媒体分类 ink bars.
      Column(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(paperColors.line))
        Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 13.dp, bottom = 15.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = stringResource(R.string.watch_stats_media_breakdown),
              modifier = Modifier.alignByBaseline(),
              fontSize = 12.sp,
              letterSpacing = 2.sp,
              fontWeight = FontWeight.Bold,
              color = paperColors.inkTertiary,
            )
            Text(
              text = stringResource(R.string.watch_stats_by_watch_time),
              modifier = Modifier.alignByBaseline(),
              fontSize = 10.5.sp,
              letterSpacing = 0.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = paperColors.inkSecondary,
            )
          }
          val total = stats.videoSeconds + stats.audioSeconds
          val videoFraction = if (total > 0) stats.videoSeconds.toFloat() / total.toFloat() else 0f
          val audioFraction = if (total > 0) stats.audioSeconds.toFloat() / total.toFloat() else 0f
          val videoPercent = (videoFraction * 100f).toInt()
          val audioPercent = if (total > 0) 100 - videoPercent else 0
          CategoryInkBar(
            label = stringResource(R.string.watch_stats_video),
            detail = stringResource(R.string.profile_duration_minutes, ((stats.videoSeconds + 59) / 60)),
            percent = videoPercent,
            fraction = videoFraction,
            hot = videoPercent > 0,
            modifier = Modifier.padding(top = 11.dp),
          )
          CategoryInkBar(
            label = stringResource(R.string.watch_stats_audio),
            detail = stringResource(R.string.profile_duration_minutes, ((stats.audioSeconds + 59) / 60)),
            percent = audioPercent,
            fraction = audioFraction,
            hot = false,
            modifier = Modifier.padding(top = 11.dp),
          )
        }
      }

      // .statsec: 最近 7 天 ink-stick columns.
      Column(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(paperColors.line))
        Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 13.dp, bottom = 15.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = stringResource(R.string.watch_stats_last_seven_days),
              modifier = Modifier.alignByBaseline(),
              fontSize = 12.sp,
              letterSpacing = 2.sp,
              fontWeight = FontWeight.Bold,
              color = paperColors.inkTertiary,
            )
            Text(
              text = stringResource(R.string.watch_stats_today, formatWatchDuration(stats.days.values.lastOrNull() ?: 0L)),
              modifier = Modifier.alignByBaseline(),
              fontSize = 10.5.sp,
              letterSpacing = 0.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = paperColors.inkSecondary,
            )
          }
          WeekInkBars(days = stats.days, modifier = Modifier.padding(top = 11.dp))
        }
      }

      // 重置：小号朱砂文字键，居中收尾。
      Text(
        text = stringResource(R.string.watch_stats_reset),
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = paperColors.red,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier =
          Modifier
            .fillMaxWidth()
            .clickable(
              enabled = stats.totalSeconds > 0 || stats.sessions > 0 || stats.media.isNotEmpty(),
            ) { confirmReset = true }
            .padding(vertical = 12.dp),
      )
    }

  }

  if (confirmReset) {
    AlertDialog(
      onDismissRequest = { confirmReset = false },
      title = { Text(stringResource(R.string.watch_stats_reset)) },
      text = { Text(stringResource(R.string.watch_stats_reset_confirm)) },
      confirmButton = {
        TextButton(
          onClick = {
            confirmReset = false
            scope.launch { repository.clear() }
          },
        ) {
          Text(
            stringResource(R.string.watch_stats_reset),
            color = MaterialTheme.colorScheme.error,
          )
        }
      },
      dismissButton = {
        TextButton(onClick = { confirmReset = false }) {
          Text(stringResource(R.string.generic_cancel))
        }
      },
    )
  }
}

/** `.stcell`: oversized figure with a small unit suffix and a letter-spaced label. */
@Composable
private fun StatCell(
  value: String,
  unit: String,
  label: String,
  modifier: Modifier = Modifier,
) {
  val paperColors = LocalPaperColors.current
  Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    Row(verticalAlignment = Alignment.Bottom) {
      Text(
        text = value,
        fontSize = 27.sp,
        fontWeight = FontWeight.ExtraBold,
        color = paperColors.ink,
      )
      Text(
        text = unit,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = paperColors.inkSecondary,
        modifier = Modifier.padding(start = 3.dp, bottom = 2.dp),
      )
    }
    Text(
      text = label,
      fontSize = 10.5.sp,
      letterSpacing = 2.sp,
      color = paperColors.inkTertiary,
      modifier = Modifier.padding(top = 7.dp),
    )
  }
}

/** `.catrow`: label + percent·duration over a 7dp recessed track with the vermilion fill. */
@Composable
private fun CategoryInkBar(
  label: String,
  detail: String,
  percent: Int,
  fraction: Float,
  hot: Boolean,
  modifier: Modifier = Modifier,
) {
  val paperColors = LocalPaperColors.current
  val animatedFraction by animateFloatAsState(
    targetValue = fraction.coerceIn(0f, 1f),
    animationSpec = if (AppMotion.shouldReduceMotion()) snap() else AppMotion.Effect.Alpha,
    label = "categoryInkBar",
  )
  Column(modifier = modifier) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = label,
        modifier = Modifier.alignByBaseline(),
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Bold,
        color = paperColors.ink,
      )
      Text(
        text = "$percent% · $detail",
        modifier = Modifier.alignByBaseline(),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (hot) paperColors.red else paperColors.inkSecondary,
      )
    }
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(top = 6.dp)
          .height(7.dp)
          .paperInset(RoundedCornerShape(4.dp), paperColors),
    ) {
      if (animatedFraction > 0f) {
        Box(
          modifier =
            Modifier
              .fillMaxWidth(animatedFraction)
              .fillMaxHeight()
              .clip(RoundedCornerShape(4.dp))
              .background(Brush.horizontalGradient(listOf(Color(0xFFB23129), Color(0xFFD65444)))),
        )
      }
    }
  }
}

/** `.weekbars`: seven recessed ink-stick slots; the filled one carries the vermilion gradient. */
@Composable
private fun WeekInkBars(days: Map<String, Long>, modifier: Modifier = Modifier) {
  val paperColors = LocalPaperColors.current
  val today = LocalDate.now()
  val dates = remember(today) { (6 downTo 0).map { today.minusDays(it.toLong()) } }
  val values = dates.map { days[it.toString()] ?: 0L }
  val chartMax = (values.maxOrNull() ?: 0L).coerceAtLeast(1L)

  Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
    dates.forEachIndexed { index, date ->
      val value = values[index]
      val isToday = index == 6
      val fraction = (value.toFloat() / chartMax.toFloat()).coerceIn(0f, 1f)
      val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = if (AppMotion.shouldReduceMotion()) snap() else AppMotion.Spatial.Expressive,
        label = "weekInkBar$index",
      )
      Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
      ) {
        Box(
          modifier =
            Modifier
              .fillMaxWidth()
              .height(62.dp)
              .paperInset(RoundedCornerShape(8.dp), paperColors),
          contentAlignment = Alignment.BottomCenter,
        ) {
          if (value > 0L) {
            Box(
              modifier =
                Modifier
                  .fillMaxWidth(0.56f)
                  .widthIn(max = 19.dp)
                  .fillMaxHeight(animatedFraction.coerceAtLeast(0.08f))
                  .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                  .background(
                    Brush.verticalGradient(listOf(Color(0xFFE06A50), Color(0xFFB23129), Color(0xFF9C2A22))),
                  ),
            )
          } else {
            Box(
              modifier =
                Modifier
                  .padding(bottom = 5.dp)
                  .size(5.dp)
                  .background(paperColors.inkTertiary.copy(alpha = 0.45f), CircleShape),
            )
          }
        }
        Text(
          text = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
          fontSize = 10.sp,
          fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
          color = if (isToday) paperColors.red else paperColors.inkTertiary,
        )
      }
    }
  }
}

@Composable
private fun formatWatchDuration(seconds: Long): String {
  if (seconds <= 0L) return stringResource(R.string.profile_duration_minutes, 0)
  val hours = seconds / 3600
  val minutes = (seconds % 3600) / 60
  val remainingSeconds = seconds % 60

  return when {
    hours > 0 && minutes > 0 -> stringResource(R.string.profile_duration_hours_minutes, hours, minutes)
    hours > 0 -> stringResource(R.string.profile_duration_hours, hours)
    minutes > 0 -> stringResource(R.string.profile_duration_minutes, minutes)
    else -> stringResource(R.string.profile_duration_seconds, remainingSeconds)
  }
}
