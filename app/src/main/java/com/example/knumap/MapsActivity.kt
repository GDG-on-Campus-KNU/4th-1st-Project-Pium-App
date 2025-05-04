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
import android.app.VoiceInteractor
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.graphics.Color
import android.graphics.Matrix
import android.widget.EditText
import android.widget.LinearLayout
import com.example.knumap.model.BaseResponse
import com.example.knumap.model.PostInfo
import com.example.knumap.model.PostInfoResponse
import com.example.knumap.model.PostPagingResponse
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.MessageDigest
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


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
    private val expandedHeight = 1300f // 확장된 높이
    private val collapsedHeight = 300f // 접힌 높이
    private var isDragging = false
    private lateinit var photoAdapter: PhotoAdapter
    private val photoList = mutableListOf<Uri>()
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_WRITE_POST = 2
    private val REQUEST_VIEW_POST = 3
    private lateinit var photoUri: Uri
    private lateinit var photoFile: File
    private var latestLocation: Location? = null  // 클래스 멤버 변수로 선언
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private var isBuildingClicked = false
    private var searchMarker: Marker? = null
    private var previousStateExpanded = false
    private lateinit var cameraButtonCollapsed: ImageButton
    private lateinit var cameraButtonExpanded: ImageButton
    private var allPostList: List<PostInfo> = emptyList()  // 전역 변수로 게시글 전체 저장
    private val currentPetalMarkers = mutableListOf<Marker>()

    val b501 = LatLngBounds.Builder()
        .include(LatLng( 35.886875+0.0001, 128.609322-0.0001))
        .include(LatLng(35.886875-0.0001, 128.609322+0.0001))
        .build()
    val b502 = LatLngBounds.Builder()
        .include(LatLng( 35.886847+0.0001, 128.609987-0.0001))
        .include(LatLng(35.886847-0.0001, 128.609987+0.0001))
        .build()
    val b503 = LatLngBounds.Builder()
        .include(LatLng( 35.886854+0.0001, 128.610716-0.0001))
        .include(LatLng(35.886854-0.0001, 128.610716+0.0001))
        .build()
    val b504 = LatLngBounds.Builder()
        .include(LatLng(35.886207+0.0001, 128.609519-0.0001))
        .include(LatLng(35.886207-0.0001, 128.609519+0.0001))
        .build()
    val b509 = LatLngBounds.Builder()
        .include(LatLng( 35.885579+0.0001, 128.609216-0.0001))
        .include(LatLng(35.885579-0.0001, 128.609216+0.0001))
        .build()
    val b510 = LatLngBounds.Builder()
        .include(LatLng( 35.886370+0.0001, 128.612199-0.0001))
        .include(LatLng(35.886370-0.0001, 128.612199+0.0001))
        .build()
    val b511 = LatLngBounds.Builder()
        .include(LatLng( 35.886324+0.0001, 128.612650-0.0001))
        .include(LatLng(35.886324-0.0001, 128.612650+0.0001))
        .build()


    val b421 = LatLngBounds.Builder()
        .include(LatLng(35.886701+0.0001, 128.613652-0.0001) )// 좌상단
        .include(LatLng(35.886701-0.0001, 128.613652+0.0001)) // 우하단
        .build()

    val b420 = LatLngBounds.Builder()
        .include(LatLng(35.886788+0.0001, 128.613163-0.0001))
        .include(LatLng(35.886788-0.0001, 128.613163+0.0001))
        .build()
    val b419 = LatLngBounds.Builder()
        .include(LatLng(35.886713+0.0001, 128.611665-0.0001))
        .include(LatLng(35.886713-0.0001, 128.611665+0.0001))
        .build()
    val b418 = LatLngBounds.Builder()
        .include(LatLng(35.887512+0.0001, 128.612741-0.0001))
        .include(LatLng(35.887512-0.0001, 128.612741+0.0001))
        .build()
    val b417 = LatLngBounds.Builder()
        .include(LatLng(35.887790+0.0001, 128.612411-0.0001))
        .include(LatLng(35.887790-0.0001, 128.612411+0.0001))
        .build()

    val b416 = LatLngBounds.Builder()
        .include(LatLng(35.887488+0.0001, 128.611654-0.0001))
        .include(LatLng(35.887488-0.0001, 128.611654+0.0001))
        .build()
    val b415 = LatLngBounds.Builder()
        .include(LatLng(35.888107+0.0001, 128.611441-0.0001))
        .include(LatLng(35.888107-0.0001, 128.611441+0.0001))
        .build()
    val b414 = LatLngBounds.Builder()
        .include(LatLng(35.888104+0.0001, 128.610890-0.0001))
        .include(LatLng(35.888104-0.0001, 128.610890+0.0001))
        .build()
    val b413 = LatLngBounds.Builder()
        .include(LatLng(35.888196+0.0001, 128.610472-0.0001))
        .include(LatLng(35.888196-0.0001, 128.610472+0.0001))
        .build()
    val b412 = LatLngBounds.Builder()
        .include(LatLng(35.887755+0.0001, 128.610714-0.0001))
        .include(LatLng(35.887755-0.0001, 128.610714+0.0001))
        .build()
    val b411 = LatLngBounds.Builder()
        .include(LatLng(35.887296+0.0001, 128.610696-0.0001))
        .include(LatLng(35.887296-0.0001, 128.610696+0.0001))
        .build()
    val b410 = LatLngBounds.Builder()
        .include(LatLng(35.887339+0.0001, 128.609702-0.0001))
        .include(LatLng(35.887339-0.0001, 128.609702+0.0001))
        .build()
    val b409 = LatLngBounds.Builder()
        .include(LatLng(35.887725+0.0001, 128.609693-0.0001))
        .include(LatLng(35.887725-0.0001, 128.609693+0.0001))
        .build()
    val b408 = LatLngBounds.Builder()
        .include(LatLng(35.888475+0.0001, 128.609894-0.0001))
        .include(LatLng(35.888475-0.0001, 128.609894+0.0001))
        .build()
    val b407 = LatLngBounds.Builder()
        .include(LatLng(35.886521+0.0001, 128.608564-0.0001))
        .include(LatLng(35.886521-0.0001, 128.608564+0.0001))
        .build()
    val b406 = LatLngBounds.Builder()
        .include(LatLng(35.886930+0.0001, 128.608417-0.0001))
        .include(LatLng(35.886930-0.0001, 128.608417+0.0001))
        .build()
    val b405 = LatLngBounds.Builder()
        .include(LatLng(35.887240+0.0001, 128.607870-0.0001))
        .include(LatLng(35.887240-0.0001, 128.607870+0.0001))
        .build()
    val b404 = LatLngBounds.Builder()
        .include(LatLng(35.887531+0.0001, 128.608509-0.0001))
        .include(LatLng(35.887531-0.0001, 128.608509+0.0001))
        .build()
    val b403 = LatLngBounds.Builder()
        .include(LatLng(35.887390+0.0001, 128.607755-0.0001))
        .include(LatLng(35.887390-0.0001, 128.607755+0.0001))
        .build()
    val b402 = LatLngBounds.Builder()
        .include(LatLng(35.887711+0.0001, 128.607653-0.0001))
        .include(LatLng(35.887711-0.0001, 128.607653+0.0001))
        .build()
    val b401 = LatLngBounds.Builder()
        .include(LatLng(35.887994+0.0001, 128.608600-0.0001))
        .include(LatLng(35.887994-0.0001, 128.608600+0.0001))
        .build()

    val b311 = LatLngBounds.Builder()
        .include(LatLng(35.887901+0.0001, 128.615162-0.0001))
        .include(LatLng(35.887901-0.0001, 128.615162+0.0001))
        .build()


    val b310 = LatLngBounds.Builder()
        .include(LatLng(35.888532+0.0001, 128.615506-0.0001))
        .include(LatLng(35.888532-0.0001, 128.615506+0.0001))
        .build()
    val b309 = LatLngBounds.Builder()
        .include(LatLng(35.889288+0.0001, 128.616073-0.0001))
        .include(LatLng(35.889288-0.0001, 128.616073+0.0001))
        .build()
    val b308 = LatLngBounds.Builder()
        .include(LatLng(35.889183+0.0001, 128.615602-0.0001))
        .include(LatLng(35.889183-0.0001, 128.615602+0.0001))
        .build()

    val b307 = LatLngBounds.Builder()
        .include(LatLng(35.889980+0.0001, 128.615805-0.0001))
        .include(LatLng(35.889980-0.0001, 128.615805+0.0001))
        .build()
    val b306 = LatLngBounds.Builder()
        .include(LatLng(35.888697+0.0001, 128.613759-0.0001))
        .include(LatLng(35.888697-0.0001, 128.613759+0.0001))
        .build()
    val b305 = LatLngBounds.Builder()
        .include(LatLng(35.889123+0.0001, 128.614215-0.0001))
        .include(LatLng(35.889123-0.0001, 128.614215+0.0001))
        .build()
    val b304 = LatLngBounds.Builder()
        .include(LatLng(35.889568+0.0001, 128.614946-0.0001))
        .include(LatLng(35.889568-0.0001, 128.614946+0.0001))
        .build()
    val b303 = LatLngBounds.Builder()
        .include(LatLng( 35.890175+0.0001, 128.615041-0.0001))
        .include(LatLng(35.890175-0.0001, 128.615041+0.0001))
        .build()
    val b302 = LatLngBounds.Builder()
        .include(LatLng(35.889908+0.0001, 128.613678-0.0001))
        .include(LatLng(35.889908-0.0001, 128.613678+0.0001))
        .build()
    val b301 = LatLngBounds.Builder()
        .include(LatLng(35.890208+0.0001, 128.613806-0.0001))
        .include(LatLng(35.890208-0.0001, 128.613806+0.0001))
        .build()
    val b219 = LatLngBounds.Builder()
        .include(LatLng(35.889211+0.0001, 128.606300-0.0001))
        .include(LatLng(35.889211-0.0001, 128.606300+0.0001))
        .build()
    val b218 = LatLngBounds.Builder()
        .include(LatLng(35.886836+0.0001, 128.607430-0.0001))
        .include(LatLng(35.886836-0.0001, 128.607430+0.0001))
        .build()
    val b217 = LatLngBounds.Builder()
        .include(LatLng(35.886841+0.0001, 128.606083-0.0001))
        .include(LatLng(35.886841-0.0001, 128.606083+0.0001))
        .build()
    val b216 = LatLngBounds.Builder()
        .include(LatLng(35.887132+0.0001, 128.604852-0.0001))
        .include(LatLng(35.887132-0.0001, 128.604852+0.0001))
        .build()
    val b215 = LatLngBounds.Builder()
        .include(LatLng(35.888279+0.0001, 128.604294-0.0001))
        .include(LatLng(35.888279-0.0001, 128.604294+0.0001))
        .build()
    val b214 = LatLngBounds.Builder()
        .include(LatLng(35.888025+0.0001, 128.605989-0.0001))
        .include(LatLng(35.888025-0.0001, 128.605989+0.0001))
        .build()
    val b213 = LatLngBounds.Builder()
        .include(LatLng(35.889220+0.0001, 128.604654-0.0001))
        .include(LatLng(35.889220-0.0001, 128.604654+0.0001))
        .build()
    val b212 = LatLngBounds.Builder()
        .include(LatLng(35.889781+0.0001, 128.605276-0.0001))
        .include(LatLng(35.889781-0.0001, 128.605276+0.0001))
        .build()
    val b211 = LatLngBounds.Builder()
        .include(LatLng(35.890202+0.0001, 128.605839-0.0001))
        .include(LatLng(35.890202-0.0001, 128.605839+0.0001))
        .build()

    val b210 = LatLngBounds.Builder()
        .include(LatLng(35.889763+0.0001, 128.606375-0.0001))
        .include(LatLng(35.889763-0.0001, 128.606375+0.0001))
        .build()
    val b209 = LatLngBounds.Builder()
        .include(LatLng(35.890259+0.0001, 128.606569-0.0001))
        .include(LatLng(35.890259-0.0001, 128.606569+0.0001))
        .build()
    val b208 = LatLngBounds.Builder()
        .include(LatLng( 35.889809+0.0001, 128.607862-0.0001))
        .include(LatLng(35.889809-0.0001, 128.607862+0.0001))
        .build()
    val b207 = LatLngBounds.Builder()
        .include(LatLng(35.889787+0.0001, 128.609203-0.0001))
        .include(LatLng(35.889787-0.0001, 128.609203+0.0001))
        .build()
    val b206 = LatLngBounds.Builder()
        .include(LatLng(35.891192+0.0001, 128.607482-0.0001))
        .include(LatLng(35.891192-0.0001, 128.607482+0.0001))
        .build()
    val b205 = LatLngBounds.Builder()
        .include(LatLng(35.890767+0.0001, 128.607038-0.0001))
        .include(LatLng(35.890767-0.0001, 128.607038+0.0001))
        .build()
    val b204 = LatLngBounds.Builder()
        .include(LatLng(35.890433+0.0001, 128.608145-0.0001))
        .include(LatLng(35.890433-0.0001, 128.608145+0.0001))
        .build()
    val b203 = LatLngBounds.Builder()
        .include(LatLng(35.890967+0.0001, 128.608307-0.0001))
        .include(LatLng(35.890967-0.0001, 128.608307+0.0001))
        .build()
    val b202 = LatLngBounds.Builder()
        .include(LatLng(35.891439+0.0001, 128.608608-0.0001))
        .include(LatLng(35.891439-0.0001, 128.608608+0.0001))
        .build()
    val b201 = LatLngBounds.Builder()
        .include(LatLng(35.891232+0.0001, 128.609506-0.0001))
        .include(LatLng(35.891232-0.0001, 128.609506+0.0001))
        .build()

    val b125 = LatLngBounds.Builder()
        .include(LatLng(35.893983+0.0001, 128.613130-0.0001))
        .include(LatLng(35.893983-0.0001, 128.613130+0.0001))
        .build()
    val b124 = LatLngBounds.Builder()
        .include(LatLng(35.893371+0.0001, 128.613397-0.0001))
        .include(LatLng(35.893371-0.0001, 128.613397+0.0001))
        .build()
    val b123 = LatLngBounds.Builder()
        .include(LatLng(35.894674+0.0001, 128.612073-0.0001))
        .include(LatLng(35.894674-0.0001, 128.612073+0.0001))
        .build()
    val b122 = LatLngBounds.Builder()
        .include(LatLng(35.894405+0.0001, 128.612400-0.0001))
        .include(LatLng(35.894405-0.0001, 128.612400+0.0001))
        .build()
    val b121 = LatLngBounds.Builder()
        .include(LatLng(35.893684+0.0001, 128.612276-0.0001))
        .include(LatLng(35.893684-0.0001, 128.612276+0.0001))
        .build()
    val b120 = LatLngBounds.Builder()
        .include(LatLng(35.893590+0.0001, 128.611273-0.0001))
        .include(LatLng(35.893590-0.0001, 128.611273+0.0001))
        .build()
    val b119 = LatLngBounds.Builder()
        .include(LatLng(35.893245+0.0001, 128.612293-0.0001))
        .include(LatLng(35.893245-0.0001, 128.612293+0.0001))
        .build()
    val b118 = LatLngBounds.Builder()
        .include(LatLng(35.892638+0.0001, 128.612365-0.0001))
        .include(LatLng(35.892638-0.0001, 128.612365+0.0001))
        .build()
    val b117 = LatLngBounds.Builder()
        .include(LatLng(35.892688+0.0001, 128.613642-0.0001))
        .include(LatLng(35.892688-0.0001, 128.613642+0.0001))
        .build()
    val b116 = LatLngBounds.Builder()
        .include(LatLng(35.892289+0.0001, 128.613272-0.0001))
        .include(LatLng(35.892289-0.0001, 128.613272+0.0001))
        .build()

    val b115 = LatLngBounds.Builder()
        .include(LatLng(35.891997+0.0001, 128.614157-0.0001))
        .include(LatLng(35.891997-0.0001, 128.614157+0.0001))
        .build()
    val b114 = LatLngBounds.Builder()
        .include(LatLng(35.891511+0.0001, 128.614865-0.0001))
        .include(LatLng(35.891511-0.0001, 128.614865+0.0001))
        .build()
    val b113 = LatLngBounds.Builder()
        .include(LatLng(35.890637+0.0001, 128.615272-0.0001))
        .include(LatLng(35.890637-0.0001, 128.615272+0.0001))
        .build()
    val b112 = LatLngBounds.Builder()
        .include(LatLng(35.890863+0.0001, 128.614511-0.0001))
        .include(LatLng(35.890863-0.0001, 128.614511+0.0001))
        .build()
    val b111 = LatLngBounds.Builder()
        .include(LatLng(35.891784+0.0001, 128.612188-0.0001))
        .include(LatLng(35.891784-0.0001, 128.612188+0.0001))
        .build()
    val b110 = LatLngBounds.Builder()
        .include(LatLng(35.891467+0.0001, 128.6136041-0.0001))
        .include(LatLng(35.891467-0.0001, 128.613604+0.0001))
        .build()
    val b109 = LatLngBounds.Builder()
        .include(LatLng(35.891437+0.0001, 128.612724-0.0001))
        .include(LatLng(35.891437-0.0001, 128.612724+0.0001))
        .build()
    val b108 = LatLngBounds.Builder()
        .include(LatLng(35.890707+0.0001, 128.611770-0.0001))
        .include(LatLng(35.890707-0.0001, 128.611770+0.0001))
        .build()
    val b107 = LatLngBounds.Builder()
        .include(LatLng(35.889698+0.0001, 128.610278-0.0001))
        .include(LatLng(35.889698-0.0001, 128.610278+0.0001))
        .build()
    val b106 = LatLngBounds.Builder()
        .include(LatLng(35.890194+0.0001, 128.610632-0.0001))
        .include(LatLng( 35.890194-0.0001, 128.610632+0.0001))
        .build()
    val b105 = LatLngBounds.Builder()
        .include(LatLng(35.890728+0.0001, 128.611045-0.0001))
        .include(LatLng(35.890728-0.0001, 128.611045+0.0001))
        .build()
    val b104 = LatLngBounds.Builder()
        .include(LatLng(35.891141+0.0001, 128.610729-0.0001))
        .include(LatLng(35.891141-0.0001, 128.610729+0.0001))
        .build()
    val b103 = LatLngBounds.Builder()
        .include(LatLng(35.891784+0.0001, 128.611233-0.0001))
        .include(LatLng(35.891784-0.0001, 128.611233+0.0001))
        .build()
    val b102 = LatLngBounds.Builder()
        .include(LatLng(35.892844+0.0001, 128.610015-0.0001))
        .include(LatLng(  35.892844-0.0001, 128.610015+0.0001))
        .build()
    val b101 = LatLngBounds.Builder()
        .include(LatLng(35.892720+0.0001, 128.610649-0.0001))
        .include(LatLng(35.892720-0.0001, 128.610649+0.0001))
        .build()
    val b100 = LatLngBounds.Builder()
        .include(LatLng( 35.890383+0.0001, 128.611978-0.0001))
        .include(LatLng(35.890383-0.0001, 128.611978+0.0001))
        .build()

    // 🔥 건물 정보를 저장하는 Map (건물 이름 → (경계 정보, 설명))
    val buildingInfoMap = mapOf(
        "공대 2호관" to Pair(b401, "이곳은 공대 2호관입니다."),
        "공대 2A호관" to Pair(b402, "이곳은 공대 2A호관입니다."),
        "P/P 구조실습실" to Pair(b403, "이곳은 P/P 구조실습실입니다."),
        "공대 1호관" to Pair(b404, "이곳은 공대 1호관입니다."),
        "연구실 안전관리센터" to Pair(b405, "이곳은 연구실 안전관리센터입니다."),
        "공대 9호관" to Pair(b406, "이곳은 공대 9호관입니다."),
        "화학관" to Pair(b407, "이곳은 화학관입니다."),
        "공대 12호관" to Pair(b408, "이곳은 공대 12호관입니다."),
        "공대 3호관" to Pair(b409, "이곳은 공대 3호관입니다."),
        "공대 6호관" to Pair(b410, "이곳은 공대 6호관입니다."),
        "공대 7호관" to Pair(b411, "이곳은 공대 7호관입니다."),
        "미래 창작관" to Pair(b412, "이곳은 미래 창작관입니다."),
        "IT대학 3호관" to Pair(b413, "이곳은 IT대학 3호관입니다."),
        "IT대학 4호관" to Pair(b414, "이곳은 IT대학 4호관입니다."),
        "IT대학 융복합공학관" to Pair(b415, "이곳은 IT대학 융복합공학관입니다."),
        "IT대학 2호관" to Pair(b416, "이곳은 IT대학 2호관입니다."),
        "반도체연구동" to Pair(b417, "이곳은 반도체연구동입니다."),
        "IT대학 1호관" to Pair(b418, "이곳은 IT대학 1호관입니다."),
        "공대 8호관" to Pair(b419, "이곳은 공대 8호관입니다."),
        "수의과대학" to Pair(b420, "이곳은 수의과대학입니다."),
        "수의과대학 부속동물병원" to Pair(b421, "이곳은 수의과대학 부속동물병원입니다."),
        "사범대학" to Pair(b301, "이곳은 사범대학입니다."),
        "교육대학원" to Pair(b302, "이곳은 교육대학원입니다."),
        "우당교육관" to Pair(b303, "이곳은 우당교육관입니다."),
        "제4합강의동" to Pair(b304, "이곳은 제4합강의동입니다."),
        "복지관" to Pair(b305, "이곳은 복지관입니다."),
        "박물관" to Pair(b306, "이곳은 박물관입니다."),
        "생활과학대학" to Pair(b307, "이곳은 생활과학대학입니다."),
        "경상대학" to Pair(b308, "이곳은 경상대학입니다."),
        "국제경상관" to Pair(b309, "이곳은 국제경상관입니다."),
        "사회과학대학" to Pair(b310, "이곳은 사회과학대학입니다."),
        "법학전문대학원" to Pair(b311, "이곳은 법학전문대학원입니다."),
        "본관" to Pair(b100, "이곳은 본관입니다."),
        "DGB문화센터" to Pair(b102, "이곳은 DGB문화센터입니다."),
        "대강당" to Pair(b101, "이곳은 대강당입니다."),
        "글로벌플라자" to Pair(b103, "이곳은 글로벌플라자입니다."),
        "인문대학" to Pair(b104, "이곳은 인문대학입니다."),
        "영성관" to Pair(b105, "이곳은 영성관입니다."),
        "인문학국제관" to Pair(b106, "이곳은 인문학국제관입니다."),
        "대학원동" to Pair(b107, "이곳은 대학원동입니다."),
        "학생종합서비스센터" to Pair(b108, "이곳은 학생종합서비스센터입니다."),
        "도서관휴게실" to Pair(b109, "이곳은 도서관휴게실입니다."),
        "정보전산원" to Pair(b110, "이곳은 정보전산원입니다."),
        "중앙도서관" to Pair(b111, "이곳은 중앙도서관입니다."),
        "어학교육원" to Pair(b112, "이곳은 어학교육원입니다."),
        "향토관" to Pair(b113, "이곳은 향토관입니다."),
        "첨성관" to Pair(b114, "이곳은 첨성관입니다."),
        "IT융합산업빌딩" to Pair(b115, "이곳은 IT융합산업빌딩입니다."),
        "종합정보센터" to Pair(b116, "이곳은 종합정보센터입니다."),
        "테크노파크" to Pair(b117, "이곳은 테크노파크입니다."),
        "약학대학" to Pair(b118, "이곳은 약학대학입니다."),
        "조형관" to Pair(b119, "이곳은 조형관입니다."),
        "예술대학" to Pair(b120, "이곳은 예술대학입니다."),
        "조소동" to Pair(b121, "이곳은 조소동입니다."),
        "문예관" to Pair(b122, "이곳은 문예관입니다."),
        "차고/중장창고" to Pair(b123, "이곳은 차고/중장창고입니다."),
        "누리관" to Pair(b124, "이곳은 누리관입니다."),
        "농생대 4호관" to Pair(b125, "이곳은 농생대 4호관입니다."),
        "농대1호관" to Pair(b201, "이곳은 농대1호관입니다."),
        "농대3호관" to Pair(b202, "이곳은 농대3호관입니다."),
        "농대2호관" to Pair(b203, "이곳은 농대2호관입니다."),
        "출판부" to Pair(b204, "이곳은 출판부입니다."),
        "복현회관" to Pair(b205, "이곳은 복현회관입니다."),
        "어린이집" to Pair(b206, "이곳은 어린이집입니다."),
        "생명공학관" to Pair(b207, "이곳은 생명공학관입니다."),
        "제1과학관" to Pair(b208, "이곳은 제1과학관입니다."),
        "자연과학대학" to Pair(b209, "이곳은 자연과학대학입니다."),
        "제2과학관" to Pair(b210, "이곳은 제2과학관입니다."),
        "국민체육센터(수영장)" to Pair(b211, "이곳은 국민체육센터(수영장)입니다."),
        "제2체육관" to Pair(b212, "이곳은 제2체육관입니다."),
        "제1체육관" to Pair(b213, "이곳은 제1체육관입니다."),
        "청룡관" to Pair(b214, "이곳은 청룡관입니다."),
        "제1학생회관(백호관)" to Pair(b215, "이곳은 제1학생회관(백호관)입니다."),
        "학군단" to Pair(b216, "이곳은 학군단입니다."),
        "생물관" to Pair(b217, "이곳은 생물관입니다."),
        "공동실험실습관(한국기초과학지원연구원)" to Pair(b218, "이곳은 공동실험실습관입니다."),
        "미래융합관" to Pair(b219, "이곳은 미래융합관입니다."),
        "진리관" to Pair(b501, "이곳은 진리관입니다."),
        "봉사관" to Pair(b502, "이곳은 봉사관입니다."),
        "화목관" to Pair(b503, "이곳은 화목관입니다."),
        "보람관" to Pair(b504, "이곳은 보람관입니다."),
        "교수아파트" to Pair(b509, "이곳은 교수아파트입니다."),
        "변전소" to Pair(b510, "이곳은 변전소입니다."),
        "창업보육센터" to Pair(b511, "이곳은 창업보육센터입니다.")

    )




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyBgHtyZ7xAdyEpIfOFnmsImPN79bwMpMyo", Locale.KOREA)
        }
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
        cameraButtonCollapsed = findViewById(R.id.cameraButtonCollapsed)
        cameraButtonExpanded = findViewById(R.id.cameraButtonExpanded)
        //
        checkAndRequestPermissions()
        /*시발 제발 되어라
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

         */
        val cameraClickListener = View.OnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        }
        cameraButtonCollapsed.setOnClickListener(cameraClickListener)
        cameraButtonExpanded.setOnClickListener(cameraClickListener)
        //
        findViewById<ImageButton>(R.id.searchButton).setOnClickListener {
            val keyword = findViewById<EditText>(R.id.searchEditText).text.toString().trim()
            val info = buildingInfoMap[keyword]

            if (info != null) {
                val bounds = info.first
                val description = info.second
                val center = bounds.center()

                // 지도 중심 이동
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(center, 18f)
                mMap.animateCamera(cameraUpdate)

                // 마커 표시
                // 이전 검색 마커가 있으면 제거
                searchMarker?.remove()

                searchMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(center)
                        .title(keyword)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.search_marker))
                        .anchor(0.5f, 1.0f)
                )


                // 하단 정보 표시
                findViewById<TextView>(R.id.locationName).text = description
            } else {
                Toast.makeText(this, "건물을 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
            }
        }


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
                    // 🔥 드래그 중 UI 전환 감지
                    val shouldBeExpanded = newHeight > (collapsedHeight + expandedHeight) / 2
                    if (shouldBeExpanded != previousStateExpanded) {
                        updateBottomSheetUI(shouldBeExpanded)  // 🔥 여기서 UI 전환
                        previousStateExpanded = shouldBeExpanded
                        Log.d("UI_DEBUG", "🔥 드래그 중 UI 전환됨: isExpanded=$shouldBeExpanded")
                    }
                }

                MotionEvent.ACTION_UP -> {
                    isDragging = false
                    val shouldExpand = bottomSheet.height > (collapsedHeight + expandedHeight) / 2
                    val targetHeight = if (bottomSheet.height > (collapsedHeight + expandedHeight) / 2) {
                        expandedHeight
                    } else {
                        collapsedHeight
                    }
                    // 🔥 최종 UI 상태 전환
                    updateBottomSheetUI(shouldExpand) // ✅ UI 전환
                    previousStateExpanded = shouldExpand
                    isExpanded = shouldExpand



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
            Log.d("DEBUG_CLICK", "PostDetailActivity로 이동 - postId: ${post.postId}")
            val intent = Intent(this, PostDetailActivity::class.java).apply {
                putExtra("postId", post.postId)
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

        // GPS 위치 가져오기 위한 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // ⏬ 바로 여기! RecyclerView가 초기화된 이후
        fetchPostListFromServer()
    }
    private fun getAccessToken(): String {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        return prefs.getString("access_token", "") ?: ""
    }

    private fun extractHashtags(text: String): List<String> {
        return Regex("#(\\w+)").findAll(text).map { it.value }.toList()
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

        updateBottomSheetUI(isExpanded)



    }
    private fun updateBottomSheetUI(isExpanded: Boolean) {
        val collapsedLayout = findViewById<LinearLayout>(R.id.collapsedLayout)
        val expandedLayout = findViewById<LinearLayout>(R.id.expandedLayout)

        if (collapsedLayout == null || expandedLayout == null) {
            Log.e("BottomSheetUI", "❌ 레이아웃이 null입니다. XML에 해당 ID가 없거나 inflate되지 않았습니다.")
            return
        }
        if (isExpanded) {
            // ✅ 확장 상태 진입
            Log.d("BottomSheetUI", "Switching to expanded layout")
            collapsedLayout?.visibility = View.GONE
            expandedLayout?.visibility = View.VISIBLE
        } else {
            // ✅ 접힘 상태 진입
            Log.d("BottomSheetUI", "Switching to collapsed layout")
            collapsedLayout?.visibility = View.VISIBLE
            expandedLayout?.visibility = View.GONE
        }

        // ✅ 상태 최종 출력
        Log.d(
            "BottomSheetUI",
            "isExpanded=$isExpanded, collapsed visibility=${collapsedLayout?.visibility}, expanded visibility=${expandedLayout?.visibility}"
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

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
            .include(LatLng(35.8959, 128.6038)) // 좌상단 GPS 좌표
            .include(LatLng(35.8850, 128.6169)) // 우하단 GPS 좌표
            .build()

        // GroundOverlay 설정
        val groundOverlayOptions = GroundOverlayOptions()
            .image(BitmapDescriptorFactory.fromResource(R.drawable.knu_custom_map)) // 커스텀 지도 이미지
            .positionFromBounds(bounds) // LatLngBounds 사용
            .transparency(0.3f) // 투명도 조정 (0 = 불투명, 1 = 완전 투명)

        // GroundOverlay 추가
        mMap.addGroundOverlay(groundOverlayOptions)

        // 지도 이동 및 줌 레벨 설정
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(knuCenter, 16f))


        //2. 내 위치 가져오기
        enableMyLocation()
        //3. 건물 추가하기
        addBuildingOverlays()
        // ✅ 🔍 카메라 줌 변경 감지 → 마커 크기 갱신
        // 🔍 카메라 이동 끝났을 때 마커 크기 갱신
        /*
        mMap.setOnCameraIdleListener {
            val zoom = mMap.cameraPosition.zoom
            val currentLatLng = latestLocation?.let { LatLng(it.latitude, it.longitude) }
            updateNearbyMarkers(currentLatLng, zoom)
        }
        */


        // 🖱 마커 클릭 시 상세 페이지 이동
        mMap.setOnMarkerClickListener { marker ->
            val postId = marker.title?.toLongOrNull()
            if (postId != null) {
                Log.d("DEBUG_MARKER", "🔍 클릭된 마커의 postId: $postId")
                val intent = Intent(this, PostDetailActivity::class.java).apply {
                    putExtra("postId", postId)
                }
                startActivity(intent)
                true
            } else {
                false
            }
        }


    }
    private fun addBuildingOverlays() {

        val buildingBoundsList = listOf(
            b501, b502, b503, b504,b509, b510,b511,
            b401, b402, b403, b404, b405,
            b406, b407, b408, b409, b410,
            b411, b412, b413, b414, b415,
            b416, b417, b418, b419, b420, b421,
            b301, b302, b303, b304, b305,
            b306, b307, b308, b309, b310,
            b311,
            b201, b202, b203, b204, b205,
            b206, b207, b208, b209, b210,
            b211, b212, b213, b214, b215,
            b216, b217, b218, b219,
            b101, b102, b103, b104, b105,
            b106, b107, b108, b109, b110,
            b111, b112, b113, b114, b115,
            b116, b117, b118, b119, b120, b121,
            b122, b123, b124, b125
        )
        //buildingOverlays["dorm"] = Pair(dormOverlay, dormBounds)
        for (bounds in buildingBoundsList) {
            drawRedBox(bounds)
        }

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
    private fun drawRedBox(bounds: LatLngBounds) {
        mMap.addPolygon(
            PolygonOptions()
                .add(
                    bounds.northeast,
                    LatLng(bounds.northeast.latitude, bounds.southwest.longitude),
                    bounds.southwest,
                    LatLng(bounds.southwest.latitude, bounds.northeast.longitude)
                )
                .fillColor(0x55FF0000.toInt())   // 반투명 빨간색
                .strokeColor(Color.BLACK)
                .strokeWidth(2f)
        )
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
                    latestLocation = location
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    Log.d("GPS_DEBUG", "현재 위치 업데이트됨: $currentLatLng")


                    updateOverlayTransparency(currentLatLng) // 위치 업데이트 시 건물과 겹치는지 확인
                    updateUserMarker(currentLatLng)
                    //updateLocationBasedInfo(location)
                    updateNearbyMarkers(currentLatLng)


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
    private fun LatLngBounds.center(): LatLng {
        return LatLng(
            (northeast.latitude + southwest.latitude) / 2,
            (northeast.longitude + southwest.longitude) / 2
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

    /*
    private fun showBuildingInfo(name: String, description: String) {
        Log.d("DEBUG", "건물 클릭됨: $name") // 로그 확인

        runOnUiThread {
            val buildingInfoTextView = findViewById<TextView>(R.id.building_info)
            buildingInfoTextView.text = "$name\n$description"
            buildingInfoTextView.visibility = View.VISIBLE // 강제로 UI 업데이트
            buildingInfoTextView.bringToFront()
        }
    }
    */
    private fun showBuildingInfo(name: String, description: String) {
        isBuildingClicked = true  // ✅ 건물 클릭 상태로 변경
        Log.d("DEBUG", "건물 클릭됨: $name")

        runOnUiThread {
            val buildingInfoTextView = findViewById<TextView>(R.id.locationName)
            //buildingInfoTextView.text = "$name\n$description"
            buildingInfoTextView.text = "$name"
            buildingInfoTextView.visibility = View.VISIBLE
            buildingInfoTextView.bringToFront()
        }
        // ✅ 일정 시간(예: 10초) 후에 다시 자동 모드로 전환
        Handler(Looper.getMainLooper()).postDelayed({
            isBuildingClicked = false
            Log.d("DEBUG", "건물 클릭 상태 해제됨 → 다시 위치 기반 장소명 표시 가능")
        }, 10000)  // 10초
    }
    private fun updateLocationBasedInfo(location: Location) {
        if (isBuildingClicked) return

        val placesClient = Places.createClient(this)

        val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS)
        val latLng = LatLng(location.latitude, location.longitude)

        val request = FindCurrentPlaceRequest.newInstance(placeFields)

        // 🔒 위치 권한 확인 필요
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("PLACE", "위치 권한이 없습니다.")
            return
        }

        placesClient.findCurrentPlace(request)
            .addOnSuccessListener { response ->
                val place = response.placeLikelihoods.firstOrNull()?.place

                val locationName = place?.name ?: "이름 없는 장소"
                val locationDetail = place?.address ?: "주소 없음"

                Log.d("PLACE", "장소명: $locationName / 주소: $locationDetail")

                runOnUiThread {
                    val buildingInfoTextView = findViewById<TextView>(R.id.locationName)
                    buildingInfoTextView.text = "$locationName"
                    buildingInfoTextView.visibility = View.VISIBLE
                    buildingInfoTextView.bringToFront()
                }
            }
            .addOnFailureListener { e ->
                Log.e("PLACE", "장소 검색 실패: ${e.message}")
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
        Log.d("DEBUG_POST", "넘어왔니?: ${requestCode}, ${resultCode}")
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            // 🔥 여기서 PostWriteActivity로 전환
            val photoUri = FileProvider.getUriForFile(
                this,
                "com.example.knumap.fileprovider",
                photoFile
            )

            // ✅ 먼저 Intent 생성
            val intent = Intent(this, PostWriteActivity::class.java).apply {
                putExtra("imageUri", photoUri.toString()) // string으로 변환해 전달
            }

            latestLocation?.let { location ->
                intent.putExtra("latitude", location.latitude)
                intent.putExtra("longitude", location.longitude)

                // ✅ Google Places SDK를 통해 장소명 가져오기
                val placesClient = Places.createClient(this)
                val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS)
                val request = FindCurrentPlaceRequest.newInstance(placeFields)

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.e("PLACE", "위치 권한이 없습니다.")
                    return
                }

                placesClient.findCurrentPlace(request)
                    .addOnSuccessListener { response ->
                        val place = response.placeLikelihoods.firstOrNull()?.place
                        val locationName = place?.name ?: "이름 없는 장소"
                        val locationDetail = place?.address ?: "주소 없음"

                        Log.d("PLACE", "장소명: $locationName / 상세주소: $locationDetail")

                        intent.putExtra("locationName", locationName)
                        intent.putExtra("locationDetail", locationDetail)

                        // 🔥 정확한 장소명 포함 후 액티비티 시작
                        startActivityForResult(intent, REQUEST_WRITE_POST)
                    }
                    .addOnFailureListener { e ->
                        Log.e("PLACE", "장소 가져오기 실패: ${e.message}")
                        intent.putExtra("locationName", "알 수 없음")
                        intent.putExtra("locationDetail", "알 수 없음")
                        startActivityForResult(intent, REQUEST_WRITE_POST)
                    }
            } ?: run {
                // 위치 정보 없을 때 fallback
                intent.putExtra("locationName", "알 수 없음")
                intent.putExtra("locationDetail", "알 수 없음")
                startActivityForResult(intent, REQUEST_WRITE_POST)
            }
        }

        if (requestCode == REQUEST_WRITE_POST && resultCode == RESULT_OK) {
            /*
            val newPost = data?.getParcelableExtra<Post>("newPost")
            if (newPost != null) {
                photoAdapter.addPhoto(newPost) // 🔥 RecyclerView에 추가
                addMarkerOnMap(newPost.imageUri) // 🔥 지도에 마커 추가
            }
            */
            fetchPostListFromServer()

        }
        if (requestCode == REQUEST_VIEW_POST && resultCode == RESULT_OK) {
            val updatedPost = data?.getParcelableExtra<Post>("updatedPost")
            updatedPost?.let {
                Log.d("DEBUG_POST", "업데이트된 post 받음: $it")
                updatePhotoLikeState(it)
            }
        }

    }

    private fun fetchPostListFromServer() {
        lifecycleScope.launch {
            try {
                val token = getAccessToken()
                val response = RetrofitClient.postService.getPostList("Bearer $token")

                val body = response.body()
                Log.d("DEBUG_POST", "✅ 전체 응답 Body 객체: $body")
                if (!response.isSuccessful) {
                    Log.e("DEBUG_POST", "🚨 게시글 목록 불러오기 실패: ${response.code()} ${response.errorBody()?.string()}")
                    return@launch
                }

                val baseResponse = response.body()
                val pagingData = baseResponse?.data

                Log.d("DEBUG_POST", "✅ 서버 원시 응답 전체: $pagingData")

                if (pagingData == null) {
                    Log.e("DEBUG_POST", "🚨 data가 null입니다.")
                    return@launch
                }

                val photoList = pagingData.content.mapNotNull { postInfo ->
                    val imageUrl = postInfo.images.firstOrNull()
                    if (imageUrl.isNullOrEmpty()) return@mapNotNull null
                    Log.d("DEBUG_POST", "서버에서 받은 postId: ${postInfo.postId}")  // ✅ 추가
                    val post = Post(
                        postId = postInfo.postId,
                        imageUri = Uri.parse(imageUrl),
                        username = postInfo.nickname,
                        locationName = postInfo.title,
                        locationDetail = "",
                        description = postInfo.content,
                        hashtags = extractHashtags(postInfo.content),
                        timestamp = System.currentTimeMillis(),
                        likes = postInfo.like,
                        isLiked = false
                    )
                    Photo(post)
                }

                Log.d("DEBUG_POST", "받은 photoList 크기: ${photoList.size}")
                photoAdapter.updatePhotoList(photoList)

                allPostList = pagingData.content

            } catch (e: Exception) {
                Log.e("DEBUG_POST", "게시글 불러오는 중 오류", e)
            }
        }
    }

    /*
    private fun fetchPostListFromServer() {
        lifecycleScope.launch {
            try {
                val token = getAccessToken()

                val rawJson = withContext(Dispatchers.IO) {
                    val client = OkHttpClient()
                    val request = Request.Builder()
                        .url("http://3.35.215.102:8080/api/post?page=0&size=10")
                        .addHeader("Authorization", "Bearer $token")
                        .build()

                    val response = client.newCall(request).execute()
                    response.body?.string()
                }

                Log.d("DEBUG_RAW_JSON", "🔥 OkHttp로 받은 원시 JSON:\n$rawJson")

            } catch (e: Exception) {
                Log.e("DEBUG_RAW_JSON", "🚨 OkHttp 요청 중 오류", e)
            }
        }

    }

     */



    /*
    private fun updateNearbyMarkers(currentLatLng: LatLng?) {

        if (currentLatLng == null || !::mMap.isInitialized) return

        // 기존 꽃잎 마커 제거
        for (marker in currentPetalMarkers) {
            marker.remove()
        }
        currentPetalMarkers.clear()

        for (post in allPostList) {
            val postLatLng = LatLng(post.latitude, post.longitude)
            val distance = FloatArray(1)
            Location.distanceBetween(
                currentLatLng.latitude, currentLatLng.longitude,
                postLatLng.latitude, postLatLng.longitude,
                distance
            )

            if (distance[0] <= 1000) {
                val deltaX = currentLatLng.longitude - postLatLng.longitude
                val deltaY = currentLatLng.latitude - postLatLng.latitude
                val angleDegrees = Math.toDegrees(atan2(deltaY, deltaX)).toFloat()

// 🔹 비트맵 원본 & 크기 조절
                val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.petal)
                val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 100, 100, false)

// 🔹 회전 기준점을 이미지 하단 중앙으로 맞추기 위한 padding
                val paddedHeight = 150
                val paddedBitmap = Bitmap.createBitmap(100, paddedHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(paddedBitmap)
                canvas.drawBitmap(scaledBitmap, 0f, paddedHeight - scaledBitmap.height.toFloat(), null)

// 🔹 하단 중앙을 기준으로 회전
                val matrix = Matrix().apply {
                    postRotate(angleDegrees, 50f, paddedHeight.toFloat())  // 100x150 이미지 기준 (50,150)은 하단 중앙
                }
                val rotatedBitmap = Bitmap.createBitmap(
                    paddedBitmap, 0, 0, paddedBitmap.width, paddedBitmap.height, matrix, true
                )

// 🔹 마커로 등록
                val descriptor = BitmapDescriptorFactory.fromBitmap(rotatedBitmap)
                val marker = mMap.addMarker(
                    MarkerOptions()
                        .position(postLatLng)
                        .icon(descriptor)
                        .anchor(0.5f, 1.0f)  // 하단 중앙이 위치에 맞게
                        .title(post.title)
                        .snippet(post.nickname)
                )
                marker?.tag = post
                marker?.let { currentPetalMarkers.add(it) }
            }
        }
    }


    private fun updateNearbyMarkers(currentLatLng: LatLng?) {
        if (currentLatLng == null || !::mMap.isInitialized) return

        for (marker in currentPetalMarkers) {
            marker.remove()
        }
        currentPetalMarkers.clear()

        for (post in allPostList) {
            val postLatLng = LatLng(post.latitude, post.longitude)
            val distance = FloatArray(1)
            Location.distanceBetween(
                currentLatLng.latitude, currentLatLng.longitude,
                postLatLng.latitude, postLatLng.longitude,
                distance
            )

            if (distance[0] <= 1000) {
                // 📍 1. 게시글 위치 마커 (빨간색)
                val baseMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(postLatLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .title("게시글 위치")
                )
                baseMarker?.let { currentPetalMarkers.add(it) }

                // ➡️ 2. 같은 위치, 회전된 방향 마커
                val angle = atan2(
                    currentLatLng.latitude - postLatLng.latitude,
                    currentLatLng.longitude - postLatLng.longitude
                )
                val angleDegrees = Math.toDegrees(angle) - 90

                // 테스트용: 방향 시각화용 파란 마커 회전 (지금은 이미지 없음이므로 회전값만 표시)
                val rotatedMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(postLatLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .rotation(angleDegrees.toFloat())  // 회전 적용
                        .flat(true) // 카메라 회전에 따라 회전됨
                        .anchor(0.5f, 1.0f)
                )
                rotatedMarker?.let { currentPetalMarkers.add(it) }
            }
        }
    }
     */

    private fun updateNearbyMarkers(currentLatLng: LatLng?) {
        if (currentLatLng == null || !::mMap.isInitialized) return

        for (marker in currentPetalMarkers) {
            marker.remove()
        }
        currentPetalMarkers.clear()

        val visibleRegion = mMap.projection.visibleRegion.latLngBounds
        for (post in allPostList) {
            val postLatLng = LatLng(post.latitude, post.longitude)
            // ✅ 위도/경도 로그 출력
            Log.d("DEBUG_MARKER", "🛰 postId: ${post.postId} → 위도: ${post.latitude}, 경도: ${post.longitude}")
            val distance = FloatArray(1)
            Location.distanceBetween(
                currentLatLng.latitude, currentLatLng.longitude,
                postLatLng.latitude, postLatLng.longitude,
                distance
            )

            if (distance[0] <= 1000) {
                // 📍 게시글 위치 마커 (빨간색)
                Log.d("DEBUG_MARKER", "📍 마커 추가 대상 - postId: ${post.postId}, 거리: ${distance[0]}m")



                // 📌 각도 계산 (위도/경도 비율 보정 포함)
                val lat1 = Math.toRadians(postLatLng.latitude)
                val lng1 = Math.toRadians(postLatLng.longitude)
                val lat2 = Math.toRadians(currentLatLng.latitude)
                val lng2 = Math.toRadians(currentLatLng.longitude)

                val deltaLat = lat2 - lat1
                val deltaLng = (lng2 - lng1) * cos((lat1 + lat2) / 2)  // 경도 보정

                val angleRadians = atan2(deltaLng, deltaLat)
                val angleDegrees = Math.toDegrees(angleRadians) - 180.0

                val bitmap = BitmapFactory.decodeResource(resources, R.drawable.petal)
                val smallMarker = Bitmap.createScaledBitmap(bitmap, 50, 50, false)
                // 📘 파란 마커 (나를 바라보는 방향)
                val marker = mMap.addMarker(
                    MarkerOptions()
                        .position(postLatLng)
                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                        .rotation(angleDegrees.toFloat())
                        .flat(true)
                        .anchor(0.5f, 0.8f)
                        .title(post.postId.toString())
                        .zIndex(post.postId.toFloat()) // 🔽 겹침 방지용
                )
                marker?.let {
                    currentPetalMarkers.add(it)
                    Log.d("DEBUG_MARKER", "🌸 꽃잎 마커 추가됨 - postId: ${post.postId}, 회전각: ${angleDegrees.toFloat()}°")

                    // 🔍 마커가 현재 화면 내에 보이는지도 확인
                    val isVisible = visibleRegion.contains(it.position)
                    Log.d("DEBUG_VISIBILITY", "🧭 마커 postId: ${post.postId} → 화면 내 보임 여부: $isVisible")
                }
                // ✅ 최종 마커 수 확인
                Log.d("DEBUG_MARKER", "🔢 최종 추가된 마커 수: ${currentPetalMarkers.size}")
            }
        }
    }
    /*
    // ✅ 3. 비트맵 리사이즈 함수 추가
    private fun resizeMarkerBitmap(resourceId: Int, scale: Float): Bitmap {
        val original = BitmapFactory.decodeResource(resources, resourceId)
        val width = (original.width * scale).toInt()
        val height = (original.height * scale).toInt()
        return Bitmap.createScaledBitmap(original, width, height, false)
    }
    */
    /*
    private fun resizeMarkerBitmap(resourceId: Int, scale: Float): Bitmap {
        val original = BitmapFactory.decodeResource(resources, resourceId)
        val maxDimension = 200  // 최대 크기 제한
        val width = (original.width * scale).coerceAtMost(maxDimension.toFloat()).toInt()
        val height = (original.height * scale).coerceAtMost(maxDimension.toFloat()).toInt()
        return Bitmap.createScaledBitmap(original, width, height, false)
    }
    */



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
    /*
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
    */




}