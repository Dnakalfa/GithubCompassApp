package br.com.convetec.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.convetec.githubsearch.R
import br.com.convetec.githubsearch.data.GitHubService
import br.com.convetec.githubsearch.domain.Repository
import br.com.convetec.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var nomeUsuario: EditText
    private lateinit var btnConfirmar: Button
    private lateinit var listaRepositories: RecyclerView
    private lateinit var githubApi: GitHubService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        showUserName()
        setupView()
        setupRetrofit()
        setupListeners()
        showUserName()
    }

    // Método responsável por realizar o setup da view e recuperar os Ids do layout
    private fun setupView() {
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
        savedUsername?.let {
            nomeUsuario.setText(it)
            getAllReposByUserName(it)
        }
    }

    // Método responsável por fazer a configuração base do Retrofit
    private fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        githubApi = retrofit.create(GitHubService::class.java)
    }

    // Método responsável por buscar todos os repositórios do usuário fornecido
    private fun getAllReposByUserName(username: String) {
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
    private fun setupAdapter(list: List<Repository>) {
        val adapter = RepositoryAdapter(
            list,
            onItemClick = { openBrowser(it.htmlUrl) },
            onShareClick = { shareRepositoryLink(it.htmlUrl) }
        )
        listaRepositories.adapter = adapter
    }

    // Método responsável por compartilhar o link do repositório selecionado
    private fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // Método responsável por abrir o browser com o link informado do repositório
    private fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )
    }
}

