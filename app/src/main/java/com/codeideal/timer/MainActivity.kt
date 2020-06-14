package com.codeideal.timer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.drawShadow
import androidx.ui.core.setContent
import androidx.ui.foundation.*
import androidx.ui.foundation.shape.corner.CircleShape
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.material.Button
import androidx.ui.material.Divider
import androidx.ui.text.TextStyle
import androidx.ui.text.style.TextAlign
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import com.codeideal.timer.ui.TimerTheme
import com.codeideal.timer.ui.divider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.TickerMode
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TimerTheme {
                Timer()
            }
        }
    }
}


@Composable
fun Timer() {
    val time = state { 0L }
    val laps = state { emptyArray<Long>() }
    val timing = state { false }
    var ticker: ReceiveChannel<Unit> = ticker(10, 0, Dispatchers.Default, TickerMode.FIXED_PERIOD)
    var tickerJob: Job? = null
    Box(modifier = Modifier.fillMaxSize(), gravity = ContentGravity.Center) {
        Box(
            modifier = Modifier.size(160.dp).drawShadow(
                elevation = 8.dp,
                shape = CircleShape
            ),
            gravity = ContentGravity.Center,
            backgroundColor = Color.Cyan
        ) {
            Text(
                text = time.value.toTime,
                style = TextStyle(
                    color = Color.Magenta,
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        VerticalScroller(
            scrollerPosition = ScrollerPosition(initial = 0f, isReversed = true),
            modifier = Modifier.heightIn(
                minHeight = 100.dp,
                maxHeight = 200.dp
            ) + Modifier.fillMaxWidth()
        ) {
            Box(gravity = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp).wrapContentSize(),
                    horizontalGravity = Alignment.CenterHorizontally
                ) {
                    laps.value.forEachIndexed { index, l ->
                        Text(
                            text = "$index: ${l.toTime}",
                            style = TextStyle(textAlign = TextAlign.Center),
                            modifier = Modifier.wrapContentSize()
                        )
                        if (index < laps.value.size - 1) {
                            Divider(modifier = Modifier.padding(horizontal = 150.dp),color = divider)
                        }
                    }
                }
            }
        }


        Divider()

        Row(modifier = Modifier.padding(16.dp) + Modifier.fillMaxWidth()) {
            Button(
                text = { Text(text = if (!timing.value) "Start" else "Stop") },
                modifier = Modifier.gravity(Alignment.CenterVertically)
                        + Modifier.weight(1f)
                        + Modifier.padding(8.dp),
                onClick = {
                    if (!timing.value) {
                        GlobalScope.launch(Dispatchers.Default) {
                            if (ticker.isClosedForReceive) {
                                ticker = ticker(10, 0, Dispatchers.Default, TickerMode.FIXED_PERIOD)
                            }
                            tickerJob = GlobalScope.launch(Dispatchers.Main) {
                                timing.value = true
                                for (i in ticker) {
                                    time.value += 1
                                }
                            }
                        }
                    } else {
                        timing.value = false
                        ticker.cancel()
                    }
                }
            )

            Button(
                text = { Text("LAP") },
                modifier = Modifier.gravity(Alignment.CenterVertically)
                        + Modifier.weight(1f)
                        + Modifier.padding(8.dp),
                onClick = {
                    laps.value += time.value
                }
            )

            Button(
                text = { Text("Reset") },
                modifier = Modifier.gravity(Alignment.CenterVertically)
                        + Modifier.weight(1f)
                        + Modifier.padding(8.dp),
                onClick = {
                    tickerJob?.cancel()
                    ticker.cancel()
                    timing.value = false
                    laps.value = emptyArray()
                    time.value = 0
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TimerTheme {
        Timer()
    }
}

val timerFormat = DecimalFormat("00")
inline val Long.toTime: String
    get() {
        val second = this * 10 / 1000
        val minute = second / 60
        return timerFormat.format(minute) +
                ":${timerFormat.format(second % 60)}" +
                ".${timerFormat.format(this % 100)}"
    }