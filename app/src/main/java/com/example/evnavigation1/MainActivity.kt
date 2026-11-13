package com.example.evnavigation1

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.skt.tmap.TMapPoint
import com.skt.tmap.TMapView
import com.skt.tmap.overlay.TMapMarkerItem
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable


// 전기차 충전소 데이터 모델
data class EVStation(
    val id: String,
    val statNm: String,
    val addr: String,
    val lat: Double,
    val lon: Double,
    val fastAvailable: Int,
    val fastTotal: Int,
    val slowAvailable: Int,
    val slowTotal: Int,
    val chgerType: String = "급속/완속",
    val useTime: String = "24시간 이용가능"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") { LoginScreen(navController) }
                    composable("mainMap") { MainMapScreen(navController) }
                    composable("routeSearch") { RouteSearchScreen(navController) }
                    composable("stationRecommend") { StationRecommendScreen(navController) }
                    composable("stationDetail/{stationId}") { backStackEntry ->
                        val stationId = backStackEntry.arguments?.getString("stationId") ?: "1"
                        StationDetailScreen(navController, stationId)
                    }
                }
            }
        }
    }
}

// 0️⃣ 로그인 / 회원가입 화면
@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚡ EV Navigation",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0984E3)
        )
        Text(
            text = "전기차 전용 최적 충전 경로 네비게이션",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp, bottom = 40.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("아이디 (이메일)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        val response = RetrofitClient.backend.login()
                        if (response.isSuccessful) {
                            Toast.makeText(context, "로그인 성공! (서버 인증 완료)", Toast.LENGTH_SHORT).show()
                            navController.navigate("mainMap") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "서버 응답 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "서버 연결 실패: 백엔드 서버를 켜주세요.", Toast.LENGTH_LONG).show()
                    }
                }
            },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0984E3)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "로그인", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { }) {
            Text(text = "회원가입하기", color = Color.Gray)
        }
    }
}

// 1️⃣ 메인 지도 화면
@Composable
fun MainMapScreen(navController: NavController) {
    var batteryLevel by remember { mutableIntStateOf(65) }
    val context = LocalContext.current

    var tMapViewInstance by remember { mutableStateOf<TMapView?>(null) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val daejeonUnivLat = 36.335973
    val daejeonUnivLon = 127.457652

    var selectedStation by remember { mutableStateOf<EVStation?>(null) }

    // 서버 데이터를 담을 동적 상태 바구니 (하드코딩 0개)
    var stationList by remember { mutableStateOf<List<EVStation>>(emptyList()) }

    // 화면 시작 시 대전대 좌표를 우리 백엔드로 보내 충전소 실시간 수신
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.backend.getStations(lat = daejeonUnivLat, lng = daejeonUnivLon)
            stationList = response.mapIndexed { idx, item ->
                EVStation(
                    id = "station_$idx",
                    statNm = item.name,
                    addr = item.address,
                    lat = item.lat,
                    lon = item.lng,
                    fastAvailable = if (item.available) 2 else 0,
                    fastTotal = 4,
                    slowAvailable = if (item.available) 4 else 0,
                    slowTotal = 6,
                    chgerType = if (item.available) "급속 가능" else "사용 불가"
                )
            }
            Toast.makeText(context, "서버에서 주변 충전소 정보를 불러왔습니다.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "서버 연결 실패: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // 서버에서 데이터가 도착하면 TMAP 지도에 마커 등록
    LaunchedEffect(stationList, tMapViewInstance) {
        val tMap = tMapViewInstance ?: return@LaunchedEffect
        if (stationList.isNotEmpty()) {
            tMap.post {
                stationList.forEach { station ->
                    val marker = TMapMarkerItem().apply {
                        id = station.id
                        tMapPoint = TMapPoint(station.lat, station.lon)
                        name = station.statNm
                    }
                    tMap.addTMapMarkerItem(marker)
                }
            }
        }
    }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val targetLat = savedStateHandle?.get<Double>("selected_lat")
    val targetLon = savedStateHandle?.get<Double>("saved_selected_lon")
        ?: savedStateHandle?.get<Double>("selected_lon")
    val targetName = savedStateHandle?.get<String>("selected_name")

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (isGranted) {
            Toast.makeText(context, "위치 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                TMapView(ctx).apply {
                    setSKTMapApiKey("sDAEBi80t588hNSM41CIX2KePKM5SBViabdydGZG")

                    post {
                        setCenterPoint(daejeonUnivLon, daejeonUnivLat, false)
                        try {
                            setZoomLevel(14)
                        } catch (e: Exception) {
                            zoomLevel = 14
                        }

                    }
                    tMapViewInstance = this
                }
            },

            update = { tMap ->
                if (targetLat != null && targetLon != null) {
                    val realTargetLat = if (targetLat > 100) targetLon else targetLat
                    val realTargetLon = if (targetLon < 100) targetLat else targetLon

                    val startPoint = TMapPoint(daejeonUnivLat, daejeonUnivLon)
                    val destPoint = TMapPoint(realTargetLat, realTargetLon)

                    tMap.post {
                        tMap.removeTMapMarkerItem("destination_marker")
                        tMap.removeAllTMapPolyLine()

                        val destinationMarker = TMapMarkerItem().apply {
                            id = "destination_marker"
                            tMapPoint = destPoint
                            name = targetName ?: "목적지"
                        }
                        tMap.addTMapMarkerItem(destinationMarker)
                    }

                    com.skt.tmap.TMapData().findPathData(startPoint, destPoint) { polyLine ->
                        if (polyLine != null) {
                            polyLine.lineColor = android.graphics.Color.BLUE
                            polyLine.lineWidth = 8f

                            tMap.post {
                                tMap.addTMapPolyLine(polyLine)

                                val centerLat = (daejeonUnivLat + realTargetLat) / 2
                                val centerLon = (daejeonUnivLon + realTargetLon) / 2

                                tMap.setCenterPoint(centerLon, centerLat, true)
                                try {
                                    tMap.zoomLevel = 13
                                } catch (e: Exception) {
                                    tMap.setZoomLevel(13)
                                }
                            }
                        }
                    }

                    savedStateHandle?.remove<Double>("selected_lat")
                    savedStateHandle?.remove<Double>("selected_lon")
                    savedStateHandle?.remove<String>("selected_name")
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 🎯 마커 터치 시 화면 하단에서 올라오는 충전소 정보 카드 (바텀시트)
        AnimatedVisibility(
            visible = selectedStation != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            selectedStation?.let { station ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = station.statNm,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "✕",
                                fontSize = 18.sp,
                                color = Color.Gray,
                                modifier = Modifier.clickable { selectedStation = null }
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = station.addr,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("🚀 급속 충전기", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${station.fastAvailable} / ${station.fastTotal} 대",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0369A1)
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("🔌 완속 충전기", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${station.slowAvailable} / ${station.slowTotal} 대",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF334155)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 경로 안내 버튼
                        Button(
                            onClick = {
                                val tMap = tMapViewInstance ?: return@Button
                                val startPoint = TMapPoint(daejeonUnivLat, daejeonUnivLon)
                                val destPoint = TMapPoint(station.lat, station.lon)

                                tMap.post {
                                    tMap.removeTMapMarkerItem("destination_marker")
                                    tMap.removeAllTMapPolyLine()

                                    val destinationMarker = TMapMarkerItem().apply {
                                        id = "destination_marker"
                                        tMapPoint = destPoint
                                        name = station.statNm
                                    }
                                    tMap.addTMapMarkerItem(destinationMarker)
                                }

                                com.skt.tmap.TMapData().findPathData(startPoint, destPoint) { polyLine ->
                                    if (polyLine != null) {
                                        polyLine.lineColor = android.graphics.Color.BLUE
                                        polyLine.lineWidth = 8f
                                        tMap.post {
                                            tMap.addTMapPolyLine(polyLine)
                                            tMap.setCenterPoint((daejeonUnivLon + station.lon) / 2, (daejeonUnivLat + station.lat) / 2, true)
                                            try { tMap.zoomLevel = 13 } catch (e: Exception) { tMap.setZoomLevel(13) }
                                        }
                                    }
                                }
                                selectedStation = null
                                Toast.makeText(context, "${station.statNm}(으)로 경로를 안내합니다.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                        ) {
                            Text("이 충전소로 경로 안내", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 상단 배터리 카드 및 목적지 검색 버튼
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E272E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚡ 현재 배터리: $batteryLevel%",
                        color = Color(0xFF00D8D6),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "예상 주행 가능 거리: 약 ${(batteryLevel * 3.8).toInt()} km",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }

            Button(
                onClick = { navController.navigate("routeSearch") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "🔍 어디로 이동하시나요? (목적지 검색)",
                    color = Color.Gray,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // 🎯 내 위치 FloatingActionButton
        FloatingActionButton(
            onClick = {
                val tMap = tMapViewInstance ?: run {
                    Toast.makeText(context, "지도가 아직 준비되지 않았습니다.", Toast.LENGTH_SHORT).show()
                    return@FloatingActionButton
                }

                val hasFinePermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                val hasCoarsePermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasFinePermission && !hasCoarsePermission) {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                    return@FloatingActionButton
                }

                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        val validLocation = location ?: return@addOnSuccessListener

                        val rawLat = validLocation.latitude
                        val rawLon = validLocation.longitude

                        if (rawLat == 0.0 || rawLon == 0.0) return@addOnSuccessListener

                        val realLat = if (rawLat > 100) rawLon else rawLat
                        val realLon = if (rawLon < 100) rawLat else rawLon

                        tMap.post {
                            tMap.setCenterPoint(realLon, realLat, true)
                            tMap.zoomLevel = 16

                            tMap.removeTMapMarkerItem("my_location_marker")
                            val myMarker = TMapMarkerItem().apply {
                                id = "my_location_marker"
                                tMapPoint = TMapPoint(realLat, realLon)
                                name = "내 위치"
                            }
                            tMap.addTMapMarkerItem(myMarker)
                            Toast.makeText(context, "내 위치로 이동했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(context, "위치를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            },
            containerColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Text(text = "🎯", fontSize = 14.sp)
        }
    }



    // 3️⃣ 경로 중간 추천 충전소 화면
    @Composable
    fun StationRecommendScreen(navController: NavController) {
        val recommendedStations = remember {
            listOf(
                EVStation(
                    id = "1",
                    statNm = "대전 대덕 Rest Stop 충전소",
                    addr = "경로 상 42km 지점",
                    lat = 36.3530,
                    lon = 127.3880,
                    fastAvailable = 2,
                    fastTotal = 4,
                    slowAvailable = 0,
                    slowTotal = 0,
                    useTime = "이용 가능 (2/4대)",
                    chgerType = "100kW 급속"
                ),
                EVStation(
                    id = "2",
                    statNm = "신탄진 휴게소 충전소",
                    addr = "경로 상 58km 지점",
                    lat = 36.4480,
                    lon = 127.4120,
                    fastAvailable = 1,
                    fastTotal = 2,
                    slowAvailable = 0,
                    slowTotal = 0,
                    useTime = "이용 가능 (1/2대)",
                    chgerType = "350kW 초고속"
                ),
                EVStation(
                    id = "3",
                    statNm = "청주 휴게소 충전소",
                    addr = "경로 상 75km 지점",
                    lat = 36.5210,
                    lon = 127.3910,
                    fastAvailable = 3,
                    fastTotal = 4,
                    slowAvailable = 0,
                    slowTotal = 0,
                    useTime = "이용 가능 (3/4대)",
                    chgerType = "100kW 급속"
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "경로 중간 추천 충전소",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3436)
            )
            Text(
                text = "목적지 도착 전 배터리 부족이 예상되어 경유지를 추천합니다.",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            recommendedStations.forEach { station ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = station.statNm,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E272E)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = station.addr, fontSize = 13.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "⚡ ${station.chgerType}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF00B894)
                            )
                            Text(
                                text = station.useTime,
                                fontSize = 13.sp,
                                color = Color(0xFF0984E3)
                            )
                        }
                    }
                }
            }
        }
    }

    // 4️⃣ 충전소 상세 정보 화면
    @Composable
    fun StationDetailScreen(navController: NavController, stationId: String) {
        val context = LocalContext.current
        var isBookmarked by remember { mutableStateOf(false) }

        val stationName = when (stationId) {
            "1" -> "대전 대덕 Rest Stop 충전소"
            "2" -> "신탄진 휴게소 충전소"
            else -> "청주 휴게소 충전소"
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "충전소 상세 정보",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F2F6)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = stationName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2F3542)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "충전 타입: 100kW / 350kW 초고속 급속",
                            fontSize = 14.sp,
                            color = Color(0xFF57606F)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "이용 가능: 현재 2대 사용 가능",
                            fontSize = 14.sp,
                            color = Color(0xFF00B894),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "주소: 대전광역시 대덕구 신탄진로 100",
                            fontSize = 14.sp,
                            color = Color(0xFF57606F)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = {
                                isBookmarked = !isBookmarked
                                val msg =
                                    if (isBookmarked) "즐겨찾기에 등록되었습니다." else "즐겨찾기가 해제되었습니다."
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isBookmarked) "즐겨찾기 해제" else "즐겨찾기 등록",
                                color = Color(0xFFFFA502),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column {
                Button(
                    onClick = {
                        Toast.makeText(context, "TMAP 길 안내를 시작합니다!", Toast.LENGTH_LONG).show()
                        navController.popBackStack("mainMap", inclusive = false)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ED573)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(text = "길 안내 시작하기", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "이전 화면으로 돌아가기", color = Color.Gray)
                }
            }
        }
    }



    // 대전대학교 주변 샘플 충전소 마커 추가 함수
    fun addSampleMarkers(tMap: TMapView) {
        val sampleStations = listOf(
            TMapMarkerItem().apply {
                id = "sample_1"
                tMapPoint = TMapPoint(36.3370, 127.4580)
                name = "⚡ 대전대 혜화관 충전소"
            },
            TMapMarkerItem().apply {
                id = "sample_2"
                tMapPoint = TMapPoint(36.3345, 127.4560)
                name = "⚡ 대전대 서문 급속충전소"
            }
        )
        sampleStations.forEach { tMap.addTMapMarkerItem(it) }
    }
}
