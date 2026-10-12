package com.example.evnavigation1

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// 검색 장소 데이터 모델
data class SearchSearchResult(
    val name: String,
    val address: String,
    val lat: Double,
    val lon: Double,
    val category: String = "전기차 충전소"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteSearchScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // 대전 주요 거점 및 더미 검색 데이터
    val dummyPlaces = remember {
        listOf(
            SearchSearchResult("대전역 충전소", "대전 동구 정동 1-1", 36.3315, 127.4328, "급속 충전 가능"),
            SearchSearchResult("대전시청 충전소", "대전 서구 둔산로 100", 36.3504, 127.3848, "완속/급속 충전소"),
            SearchSearchResult("유성온천역 충전소", "대전 유성구 봉명동 570", 36.3537, 127.3415, "급속 충전 가능"),
            SearchSearchResult("대전대학교 혜화문화관", "대전 동구 대학로 62", 36.335973, 127.457652, "학교 내 충전소"),
            SearchSearchResult("복합터미널 충전소", "대전 동구 동서대로 1689", 36.3512, 127.4371, "24시간 운영"),
            SearchSearchResult("정림동 주민센터 충전소", "대전 서구 정림로 24", 36.3075, 127.3661, "공공 충전소")
        )
    }

    // 검색어 필터링 결과
    val filteredPlaces = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            dummyPlaces
        } else {
            dummyPlaces.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.address.contains(searchQuery, ignoreCase = true)
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
            // 1. 검색 입력 필드
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("충전소명 또는 주소 검색") },
                leadingIcon = { Text("🔍", fontSize = 16.sp) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
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
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (searchQuery.isBlank()) "추천 목적지 목록" else "검색 결과 (${filteredPlaces.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            // 2. 검색 결과 목록
            if (filteredPlaces.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("검색 결과가 없습니다.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredPlaces) { place ->
                        SearchResultItem(
                            place = place,
                            onClick = {
                                // 지도 화면(MainMapScreen)으로 선택된 좌표 및 이름 전달
                                navController.previousBackStackEntry?.savedStateHandle?.apply {
                                    set("selected_lat", place.lat)
                                    set("selected_lon", place.lon)
                                    set("selected_name", place.name)
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
fun SearchResultItem(
    place: SearchSearchResult,
    onClick: () -> Unit
) {
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
                    text = place.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = place.address,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = place.category,
                fontSize = 11.sp,
                color = Color(0xFF0284C7),
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .background(Color(0xFFE0F2FE), shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}