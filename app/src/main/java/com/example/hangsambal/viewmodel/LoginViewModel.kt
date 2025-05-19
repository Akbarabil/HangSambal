package com.example.hangsambal.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.hangsambal.model.response.GetPresence
import com.example.hangsambal.model.response.SignIn
import com.example.hangsambal.network.NetworkClient
import com.example.hangsambal.util.Prefs
import com.example.hangsambal.util.State
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : BaseViewModel() {
    var stateLogin = MutableLiveData<State>()
    var isSuccessSignIn = MutableLiveData<Boolean>()
    var jwt = MutableLiveData<String?>()
    var alreadyPresence = MutableLiveData<Boolean>()

    // Fungsi untuk melakukan login
    fun signIn(context: Context, username: String, password: String) {
        // Set state login menjadi LOADING untuk menunjukkan proses sedang berjalan
        stateLogin.value = State.LOADING

        // Panggil API login melalui Retrofit
        NetworkClient().getService(context)
            .signIn(username, password)
            .enqueue(object : Callback<SignIn> {

                // Ketika respon dari server diterima
                override fun onResponse(call: Call<SignIn>, response: Response<SignIn>) {
                    // Jika login berhasil dan dataSignIn tidak null
                    if (response.body()?.dataSignIn != null) {
                        // Tandai bahwa login berhasil
                        isSuccessSignIn.value = response.isSuccessful

                        // Simpan JWT dari response ke dalam LiveData
                        jwt.value = response.body()?.dataSignIn?.jwt

                        // Lanjut ke proses cek presensi user
                        checkPresence(context)
                    } else {
                        // Jika login gagal (misal username/password salah)
                        isSuccessSignIn.value = response.isSuccessful

                        // Set state menjadi ERROR agar UI bisa menampilkan error
                        stateLogin.value = State.ERROR

                        // Simpan pesan error dari server jika ada, atau gunakan default
                        errorMessage.value = response.body()?.statusMessage
                            ?: "Terjadi kesalahan. Silakan coba lagi."
                    }
                }

                // Jika koneksi ke server gagal (misalnya tidak ada internet)
                override fun onFailure(call: Call<SignIn>, t: Throwable) {
                    // Tandai login gagal
                    isSuccessSignIn.value = false

                    // Update state menjadi ERROR agar UI tahu proses gagal
                    stateLogin.value = State.ERROR

                    // Jalankan fungsi penanganan error tambahan
                    handleFailure(t)
                }
            })
    }

    // Fungsi untuk memeriksa apakah user sudah presensi
    fun checkPresence(context: Context) {
        // Panggil API presensi, kirimkan token JWT yang sudah disimpan
        NetworkClient().getService(context)
            .getPresence(jwt.value.toString())
            .enqueue(object : Callback<GetPresence> {

                // Jika berhasil menerima respon dari server
                override fun onResponse(call: Call<GetPresence>, response: Response<GetPresence>) {
                    // Tandai bahwa proses login dan cek presensi sudah selesai
                    stateLogin.value = State.COMPLETE

                    // Jika field dataPresence tidak null
                    if (response.body()?.dataPresence != null) {
                        // Tandai apakah user sudah melakukan presensi (data tidak kosong)
                        alreadyPresence.value = response.body()?.dataPresence?.isNotEmpty()
                    } else {
                        // Kalau null, berarti user belum presensi
                        alreadyPresence.value = false
                    }

                    // Jika dataPresence tidak kosong (user sudah presensi)
                    if (!response.body()?.dataPresence.isNullOrEmpty()) {
                        // Simpan JWT ke dalam SharedPreferences
                        Prefs(context).jwt = jwt.value.toString()

                        // Simpan idDistrict dari presensi pertama user
                        Prefs(context).idDistrict =
                            response.body()?.dataPresence!!.firstOrNull()?.idDistrict
                    }
                }

                // Jika gagal terhubung ke server (misal timeout, tidak ada internet)
                override fun onFailure(call: Call<GetPresence>, t: Throwable) {
                    // Anggap user belum presensi
                    alreadyPresence.value = false

                    // Tandai status sebagai ERROR
                    stateLogin.value = State.ERROR
                }
            })
    }

}