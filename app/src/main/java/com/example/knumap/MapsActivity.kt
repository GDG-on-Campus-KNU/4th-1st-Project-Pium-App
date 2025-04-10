package com.example.knumap

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.knumap.model.Post
import com.example.knumap.network.RetrofitClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

import java.security.MessageDigest


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userMarker: Marker? = null // 사용자 위치 마커
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val CAMERA_PERMISSION_REQUEST_CODE = 1001
    private val STORAGE_PERMISSION_REQUEST_CODE = 1002
    private lateinit var locationCallback: LocationCallback
    private val buildingOverlays = mutableMapOf<String, Pair<GroundOverlay?, LatLngBounds>>()
    private lateinit var bottomSheet: NestedScrollView
    private lateinit var dragHandle: View
    private var isExpanded = false
    private lateinit var myLocationButton: ImageButton
    private lateinit var zoomInButton: ImageButton
    private lateinit var zoomOutButton: ImageButton
    private var initialY = 0f
    private var touchStartY = 0f
    private val expandedHeight = 1500f // 확장된 높이
    private val collapsedHeight = 300f // 접힌 높이
    private var isDragging = false
    private lateinit var photoAdapter: PhotoAdapter
    private val photoList = mutableListOf<Uri>()
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_WRITE_POST = 2
    private val REQUEST_VIEW_POST = 3
    private lateinit var photoUri: Uri
    private lateinit var photoFile: File
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // 1. SharedPreferences에서 access_token 꺼내기
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val jwtAccessToken = prefs.getString("access_token", null)

        if (jwtAccessToken.isNullOrBlank()) {
            Toast.makeText(this, "JWT 토큰 없음", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. 서버에 테스트 요청 보내기
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authService.getTestMessage("Bearer $jwtAccessToken")
                if (response.isSuccessful && response.body()?.success == true) {
                    val message = response.body()?.data?.message.orEmpty()
                    Toast.makeText(this@MapsActivity, "서버 응답: $message", Toast.LENGTH_LONG).show()
                    Log.d("TEST", "서버 메시지: $message")
                } else {
                    Toast.makeText(this@MapsActivity, "응답 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e("TEST", "응답 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Toast.makeText(this@MapsActivity, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("TEST", "에러: ${e.message}")
            }
        }




        // SupportMapFragment를 동적으로 추가
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_container) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().replace(R.id.map_container, it).commit()
            }

        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        myLocationButton = findViewById(R.id.myLocationButton)
        zoomInButton = findViewById(R.id.zoomInButton)
        zoomOutButton = findViewById(R.id.zoomOutButton)
        zoomInButton.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomIn())
        }
        zoomOutButton.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomOut())
        }


        myLocationButton.setOnClickListener {
            moveToCurrentLocation()
        }
        //
        checkAndRequestPermissions()
        findViewById<ImageButton>(R.id.cameraButton).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                // 🔹 카메라 실행
                openCamera()
            } else {
                // 🔹 카메라 권한 요청
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        }
        //


        // 🔥 바텀 시트 초기화
        bottomSheet = findViewById(R.id.bottomSheetContainer)
        dragHandle = findViewById(R.id.dragHandle)

        dragHandle.setOnClickListener {
            toggleBottomSheet();

        }

        // 🔥 드래그 기능 추가
        bottomSheet.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialY = bottomSheet.height.toFloat()
                    touchStartY = event.rawY
                    isDragging = true
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaY = (touchStartY - event.rawY).toInt()
                    val newHeight = (initialY + deltaY).coerceIn(collapsedHeight, expandedHeight)

                    bottomSheet.layoutParams.height = newHeight.toInt()
                    bottomSheet.requestLayout()
                }

                MotionEvent.ACTION_UP -> {
                    isDragging = false

                    val targetHeight = if (bottomSheet.height > (collapsedHeight + expandedHeight) / 2) {
                        expandedHeight
                    } else {
                        collapsedHeight
                    }

                    
                    // 🔥 ValueAnimator 적용해서 부드럽게 이동
                    val animator = ValueAnimator.ofInt(bottomSheet.height, targetHeight.toInt())
                    animator.addUpdateListener { animation ->
                        val height = animation.animatedValue as Int
                        bottomSheet.layoutParams.height = height
                        bottomSheet.requestLayout()
                    }
                    animator.duration = 300
                    animator.start()
                }
            }
            true
        }

        findViewById<ImageButton>(R.id.sortLatestButton).setOnClickListener {
            photoAdapter.sortByLatest()
        }

        findViewById<ImageButton>(R.id.sortPopularButton).setOnClickListener {
            photoAdapter.sortByPopular()
        }


        // RecyclerView 설정
        photoAdapter = PhotoAdapter(mutableListOf()) { post ->
            val intent = Intent(this, PostDetailActivity::class.java).apply {
                putExtra("newPost", post)
            }
            startActivityForResult(intent, REQUEST_VIEW_POST)
        }
        //photoAdapter = PhotoAdapter(photoList)
        val photoRecyclerView = findViewById<RecyclerView>(R.id.photoRecyclerView)

        photoRecyclerView.apply {
            layoutManager = GridLayoutManager(this@MapsActivity, 3) // ✅ 3열 그리드로 설정
            adapter = photoAdapter
            isNestedScrollingEnabled = true // ✅ 내부 스크롤 활성화
        }


        var recyclerStartY = 0f
        photoRecyclerView.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
        /*
        // ✅ RecyclerView 터치 필터: 미세 스크롤 방지
        photoRecyclerView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    recyclerStartY = event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    val delta = Math.abs(event.rawY - recyclerStartY)
                    if (delta < 20) {
                        return@setOnTouchListener true
                    }
                    v.parent.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_UP -> {
                    // ✅ 터치 해제 시 부모에게 이벤트를 넘기지 않음 (튀는 현상 방지)
                    v.parent.requestDisallowInterceptTouchEvent(true)
                }
            }
            false
        }

         */
        /*
        photoRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    recyclerView.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
        })
        */




        // GPS 위치 가져오기 위한 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun toggleBottomSheet() {
        val targetHeight = if (isExpanded) collapsedHeight else expandedHeight
        val targetButtonTranslation =  0f  // 버튼도 함께 위로 이동

        val animator = ValueAnimator.ofInt(bottomSheet.height, targetHeight.toInt())
        animator.addUpdateListener { animation ->
            val height = animation.animatedValue as Int
            bottomSheet.layoutParams.height = height
            bottomSheet.requestLayout()
        }

        // 버튼 애니메이션 (바텀 시트와 함께 이동)
        myLocationButton.animate().translationY(targetButtonTranslation).setInterpolator(
            DecelerateInterpolator()
        ).start()
        zoomInButton.animate().translationY(targetButtonTranslation).setInterpolator(
            DecelerateInterpolator()
        ).start()
        zoomOutButton.animate().translationY(targetButtonTranslation).setInterpolator(
            DecelerateInterpolator()
        ).start()

        animator.duration = 300
        animator.start()

        isExpanded = !isExpanded
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // 0.스타일 적용 (지도 타일 숨김)
        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style)
            )
            if (!success) {
                Log.e("MapActivity", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("MapActivity", "Style file not found.", e)
        }

        // 1.커스텀 이미지 씌우기
        // 경북대학교 중심 좌표
        val knuCenter = LatLng(35.8886, 128.6105)

        // 좌상단과 우하단 좌표 설정
        val bounds = LatLngBounds.Builder()
            .include(LatLng(35.8965, 128.6030)) // 좌상단 GPS 좌표
            .include(LatLng(35.8840, 128.6176)) // 우하단 GPS 좌표
            .build()

        // GroundOverlay 설정
        val groundOverlayOptions = GroundOverlayOptions()
            .image(BitmapDescriptorFactory.fromResource(R.drawable.knu_map)) // 커스텀 지도 이미지
            .positionFromBounds(bounds) // LatLngBounds 사용
            .transparency(0.6f) // 투명도 조정 (0 = 불투명, 1 = 완전 투명)

        // GroundOverlay 추가
        mMap.addGroundOverlay(groundOverlayOptions)

        // 지도 이동 및 줌 레벨 설정
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(knuCenter, 16f))

        //2. 내 위치 가져오기
        enableMyLocation()
        //3. 건물 추가하기
        addBuildingOverlays()

    }
    private fun addBuildingOverlays() {
        val libraryBounds = LatLngBounds.Builder()
            .include(LatLng(35.8889, 128.6112)) // 좌상단
            .include(LatLng(35.8881, 128.6120)) // 우하단
            .build()

        val dormBounds = LatLngBounds.Builder()
            .include(LatLng(35.8918, 128.6145))
            .include(LatLng(35.8910, 128.6155))
            .build()

        // 도서관 오버레이 추가
        val libraryOverlay = mMap.addGroundOverlay(
            GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.knu_library))
                .positionFromBounds(libraryBounds)
                .transparency(0.0f)
        )

        buildingOverlays["library"] = Pair(libraryOverlay, libraryBounds)

        // 기숙사 오버레이 추가
        val dormOverlay = mMap.addGroundOverlay(
            GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.knu_dorm))
                .positionFromBounds(dormBounds)
                .transparency(0.0f)
        )
        buildingOverlays["dorm"] = Pair(dormOverlay, dormBounds)
        // 건물 클릭 이벤트 처리 (터치 위치 감지)
        /*
        mMap.setOnMapClickListener { latLng ->
            if (libraryBounds.contains(latLng)) {
                showBuildingInfo("중앙도서관", "이곳은 경북대 중앙도서관입니다.")
            }
            else if (dormBounds.contains(latLng)) {
                showBuildingInfo("첨성관", "이곳은 경북대 첨성관입니다.")
            }
        }

        */

        // 🔥 건물 정보를 저장하는 Map (건물 이름 → (경계 정보, 설명))
        val buildingInfoMap = mapOf(
            "중앙도서관" to Pair(libraryBounds, "이곳은 경북대 중앙도서관입니다."),
            "첨성관" to Pair(dormBounds, "이곳은 경북대 첨성관입니다."),
            // 🔥 여기에 새로운 건물을 쉽게 추가 가능
            // "공대1호관" to Pair(engBuildingBounds, "이곳은 경북대 공대1호관입니다."),
            // "IT융합관" to Pair(itBuildingBounds, "이곳은 경북대 IT융합관입니다.")
        )

        mMap.setOnMapClickListener { latLng ->
            val foundBuilding = buildingInfoMap.entries.find { it.value.first.contains(latLng) }

            if (foundBuilding != null) {
                val (buildingName, buildingDescription) = foundBuilding.key to foundBuilding.value.second
                showBuildingInfo(buildingName, buildingDescription)
            } else {
                Log.d("MAP_DEBUG", "해당 위치에 건물 정보가 없음")
            }
        }




    }
    // 🔥 내 위치 감지하여 건물과 겹치는지 확인 후 투명도 변경
    private fun updateOverlayTransparency(currentLatLng: LatLng) {

        for ((key, value) in buildingOverlays) {
            val overlay = value.first
            val bounds = value.second

            if (bounds.contains(currentLatLng)) {
                Log.d("MAP_DEBUG", "현재 위치 ($currentLatLng) 가 $key 내부에 있음! → 투명도 조정")
                overlay?.setTransparency(0.5f) // 반투명
            } else {
                Log.d("MAP_DEBUG", "현재 위치 ($currentLatLng) 가 $key 밖에 있음! → 원래대로")
                overlay?.setTransparency(0.0f) // 불투명
            }


        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            mMap.isMyLocationEnabled = true
            startLocationUpdates() // 🔥 위치 업데이트 시작
        }
    }
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    Log.d("GPS_DEBUG", "현재 위치 업데이트됨: $currentLatLng")

                    updateOverlayTransparency(currentLatLng) // 위치 업데이트 시 건물과 겹치는지 확인
                    updateUserMarker(currentLatLng)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    //🔥 사용자 위치 마커 업데이트
    private fun updateUserMarker(userLatLng: LatLng) {
        userMarker?.remove()
        userMarker = mMap.addMarker(
            MarkerOptions().position(userLatLng).title("내 위치")
        )
    }
    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) { // Android 10 이하
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        } else { // Android 13 이상 (미디어 읽기 권한 필요)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    private fun checkPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        val permissionsToRequest = mutableListOf<String>()

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation()
                }
            }
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "카메라 권한 허용됨", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "저장소 접근 권한 허용됨", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "저장소 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates() // 🔥 앱이 다시 활성화되면 위치 업데이트 시작
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback) // 🔥 앱이 백그라운드로 가면 위치 업데이트 중지
    }


    private fun showBuildingInfo(name: String, description: String) {
        Log.d("DEBUG", "건물 클릭됨: $name") // 로그 확인

        runOnUiThread {
            val buildingInfoTextView = findViewById<TextView>(R.id.building_info)
            buildingInfoTextView.text = "$name\n$description"
            buildingInfoTextView.visibility = View.VISIBLE // 강제로 UI 업데이트
            buildingInfoTextView.bringToFront()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            enableMyLocation()
        } else {
            // 사용자가 권한을 거부했을 때 처리할 로직 추가
        }
    }
    // 🔥 현재 위치로 이동하는 기능 추가
    private fun moveToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                }
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    private fun openCamera() {
        Log.d("DEBUG_CAMERA", "카메라 실행 시도")  // 카메라 실행 로그 추가

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) == null) {
            Log.e("DEBUG_CAMERA", "카메라 앱을 실행할 수 없음!")
            return
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (storageDir == null) {
            Log.e("DEBUG_CAMERA", "스토리지 디렉토리를 찾을 수 없음")
            return
        }

        photoFile = File(storageDir, "KNU_$timeStamp.jpg")

        val photoUri = FileProvider.getUriForFile(
            this,
            "com.example.knumap.fileprovider",
            photoFile
        )

        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

        try {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            Log.d("DEBUG_CAMERA", "카메라 실행 성공")
        } catch (e: Exception) {
            Log.e("DEBUG_CAMERA", "카메라 실행 중 오류 발생: ${e.message}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("DEBUG_POST", "넘어괸왔니?: ${requestCode}, ${resultCode}")
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            // 🔥 여기서 PostWriteActivity로 전환
            val photoUri = FileProvider.getUriForFile(
                this,
                "com.example.knumap.fileprovider",
                photoFile
            )

            val intent = Intent(this, PostWriteActivity::class.java).apply {
                putExtra("imageUri", photoUri.toString()) // string으로 변환해 전달
            }
            startActivityForResult(intent, REQUEST_WRITE_POST)


        }
        if (requestCode == REQUEST_WRITE_POST && resultCode == RESULT_OK) {
            val newPost = data?.getParcelableExtra<Post>("newPost")
            if (newPost != null) {
                photoAdapter.addPhoto(newPost) // 🔥 RecyclerView에 추가
                addMarkerOnMap(newPost.imageUri) // 🔥 지도에 마커 추가
            }
        }
        if (requestCode == REQUEST_VIEW_POST && resultCode == RESULT_OK) {
            val updatedPost = data?.getParcelableExtra<Post>("updatedPost")
            updatedPost?.let {
                Log.d("DEBUG_POST", "업데이트된 post 받음: $it")
                updatePhotoLikeState(it)
            }
        }

    }
    private fun updatePhotoLikeState(updatedPost: Post) {
        Log.d("DEBUG_POST", "업데이트 요청 받은 post: ${updatedPost.imageUri}, likes=${updatedPost.likes}, isLiked=${updatedPost.isLiked}")

        val photoList = photoAdapter.getPhotoList() // photoList 접근 가능한 방식이어야 함
        Log.d("DEBUG_POST", "현재 photoList 크기: ${photoList.size}")

        val index = photoList.indexOfFirst {
            Log.d("DEBUG_POST", "검사 중: ${it.uri} == ${updatedPost.imageUri}")
            it.uri == updatedPost.imageUri
        }

        if (index != -1) {
            val photo = photoList[index]
            Log.d("DEBUG_POST", "일치하는 사진 찾음. 기존 좋아요: ${photo.likes}, isLiked=${photo.isLiked}")

            photo.likes = updatedPost.likes
            photo.isLiked = updatedPost.isLiked
            photoAdapter.notifyItemChanged(index)

            Log.d("DEBUG_POST", "업데이트 완료 at index $index: 새 좋아요 ${photo.likes}, isLiked=${photo.isLiked}")
        } else {
            Log.w("DEBUG_POST", "⚠️ Photo 리스트에서 일치하는 게시물 못 찾음 - ${updatedPost.imageUri}")
        }
    }


    private fun addMarkerOnMap(photoUri: Uri) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // ✅ 위치 권한이 있을 경우 현재 위치 가져오기
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title("찍은 사진")
                            .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromUri(photoUri)))
                    )
                } else {
                    Toast.makeText(this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // ❌ 위치 권한이 없으면 요청
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        //return MediaStore.Images.Media.getBitmap(contentResolver, uri)

        val originalBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)

        // ✅ 마커 크기에 맞게 비트맵 크기 조정 (100x100 픽셀)
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 100, 100, false)

        return scaledBitmap

    }

    // 🔥 내부 저장소에 촬영한 사진 저장하는 함수
    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File(storageDir, "KNU_$timeStamp.jpg")

        try {
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            return imageFile.absolutePath
        } catch (e: IOException) {
            Log.e("Camera", "사진 저장 실패: ${e.message}")
            return ""
        }
    }



}