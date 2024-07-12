package br.com.convetec.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.convetec.githubsearch.data.GitHubService
import br.com.convetec.githubsearch.domain.Repository
import br.com.convetec.githubsearch.ui.adapter.RepositoryAdapter
import br.com.convetec.githubsearch.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    lateinit var nomeUsuario: EditText
    lateinit var btnConfirmar: Button
    lateinit var listaRepositories: RecyclerView
    lateinit var githubApi: GitHubService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        showUserName()
        setupRetrofit()
        setupListeners()
    }

    // Método responsável por realizar o setup da view e recuperar os Ids do layout
    fun setupView() {
        nomeUsuario = findViewById(R.id.et_nome_usuario)
        btnConfirmar = findViewById(R.id.btn_confirmar)
        listaRepositories = findViewById(R.id.rv_lista_repositories)
        listaRepositories.layoutManager = LinearLayoutManager(this)
    }

    // Método responsável por configurar os listeners click da tela
    private fun setupListeners() {
        btnConfirmar.setOnClickListener {
            val username = nomeUsuario.text?.toString()
            if (!username.isNullOrEmpty()) {
                saveUserLocal(username)
                getAllReposByUserName(username)
            }
        }
    }

    // Salvar o usuário preenchido no EditText utilizando uma SharedPreferences
    private fun saveUserLocal(username: String) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("github_username", username)
            apply()
        }
    }

    private fun showUserName() {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        val savedUsername = sharedPref.getString("github_username", null)
        if (!savedUsername.isNullOrEmpty()) {
            nomeUsuario.setText(savedUsername)
            getAllReposByUserName(savedUsername)
        }
    }

    // Método responsável por fazer a configuração base do Retrofit
    fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        githubApi = retrofit.create(GitHubService::class.java)
    }

    // Método responsável por buscar todos os repositórios do usuário fornecido
    fun getAllReposByUserName(username: String) {
        githubApi.getAllRepositoriesByUser(username).enqueue(object : Callback<List<Repository>> {
            override fun onResponse(call: Call<List<Repository>>, response: Response<List<Repository>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        setupAdapter(it)
                    }
                }
            }

            override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                // Tratar erro de API
            }
        })
    }

    // Método responsável por realizar a configuração do adapter
    fun setupAdapter(list: List<Repository>) {
        val adapter = RepositoryAdapter(
            list,
            onItemClick = { openBrowser(it.htmlUrl) },
            onShareClick = { shareRepositoryLink(it.htmlUrl) }
        )
        listaRepositories.adapter = adapter
    }

    // Método responsável por compartilhar o link do repositório selecionado
    fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // Método responsável por abrir o browser com o link informado do repositório
    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )
    }
}
