package org.example.rebuilt_dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.painterResource
import rebuilt_dashboard.composeapp.generated.resources.Res
import rebuilt_dashboard.composeapp.generated.resources.simulation_board_2026_ampty
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*

@Composable
fun dashBoard() {
    Box(
        modifier = Modifier.background(color = Color.Black)
    ) {
        Image(
            painter = painterResource(Res.drawable.simulation_board_2026_ampty),
            contentDescription = "dashboard background",
            modifier = Modifier.padding(50.dp)
        )
        val buttonL1 = AppButton(onClick = {})
        val buttonL2 = AppButton(onClick = {})
        val buttonL3 = AppButton(onClick = {})
        val buttonR1 = AppButton(onClick = {})
        val buttonR2 = AppButton(onClick = {})
        val buttonR3 = AppButton(onClick = {})


    }
}

@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .height(80.dp),
        shape = RoundedCornerShape(200.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF006994)
        )
    ) {}
}
*/