package com.app.codebuzz.flipquotes.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.codebuzz.flipquotes.data.Quote
import com.app.codebuzz.flipquotes.R

@Composable
fun CardLayout(quote: Quote) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF070505))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.placeholder),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(185.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = quote.quote,
                    fontSize = 18.sp,
                    color = Color(0xFFE0E0E0),
                    fontFamily = FontFamily.SansSerif,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                quote.author.let {
                    Text(
                        text = it,
                        fontSize = 16.sp,
                        color = Color(0xFFAAAAAA),
                        fontFamily = FontFamily.Serif,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Read More",
                    fontSize = 14.sp,
                    color = Color(0xFFFFA726)
                )
            }
        }
    }
}
