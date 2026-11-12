package com.example.evnavigation1

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteSearchScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<PoiItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // 실제 TMAP API 호출 함수
    val performSearch: () -> Unit = {
        if (searchQuery.isNotBlank()) {
            focusManager.clearFocus()
            coroutineScope.launch {
                isLoading = true
                hasSearched = true
                try {
                    val response = RetrofitClient.instance.searchPoi(keyword = searchQuery)
                    searchResults = response.searchPoiInfo?.pois?.poi ?: emptyList()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "검색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("목적지 검색", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Text("←", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 1. 검색 입력 필드 및 검색 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("충전소 또는 목적지 입력") },
                    leadingIcon = { Text("🔍", fontSize = 16.sp) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                searchResults = emptyList()
                                hasSearched = false
                            }) {
                                Text("✕", fontSize = 16.sp, color = Color.Gray)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { performSearch() })
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { performSearch() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("검색", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (!hasSearched) "원하시는 목적지를 검색해 주세요" else "실시간 검색 결과 (${searchResults.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            // 2. 검색 결과 목록
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF0284C7))
                }
            } else if (hasSearched && searchResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("검색 결과가 없습니다.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults) { poi ->
                        RealSearchResultItem(
                            poi = poi,
                            onClick = {
                                var lat = poi.frontLat.toDoubleOrNull() ?: 36.335973
                                var lon = poi.frontLon.toDoubleOrNull() ?: 127.457652

                                if (lat > 1000) lat /= 1000000.0
                                if (lon > 1000) lon /= 1000000.0
                                if (lat > lon) {
                                    val temp = lat
                                    lat = lon
                                    lon = temp
                                }

                                // 지도 화면(MainMapScreen)으로 실제 좌표 및 이름 전달
                                navController.previousBackStackEntry?.savedStateHandle?.apply {
                                    set("selected_lat", lat)
                                    set("selected_lon", lon)
                                    set("selected_name", poi.name)
                                }
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RealSearchResultItem(
    poi: PoiItem,
    onClick: () -> Unit
) {
    val fullAddress = listOf(
        poi.upperAddrName,
        poi.middleAddrName,
        poi.lowerAddrName,
        poi.roadName,
        poi.firstBuildNo,
        poi.detailAddrName
    ).filter { it.isNotBlank() }.joinToString(" ")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF0F9FF), shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("📍", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = poi.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = fullAddress.ifBlank { "주소 정보 없음" },
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}