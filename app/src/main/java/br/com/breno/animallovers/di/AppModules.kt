package br.com.breno.animallovers.di

import br.com.breno.animallovers.viewModel.LoginActivityViewModel
import br.com.breno.animallovers.viewModel.RedefinirSenhaViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel<LoginActivityViewModel> { LoginActivityViewModel() }
    viewModel<RedefinirSenhaViewModel> { RedefinirSenhaViewModel() }
}