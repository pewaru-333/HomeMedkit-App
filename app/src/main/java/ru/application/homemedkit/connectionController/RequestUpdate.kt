package ru.application.homemedkit.connectionController

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.application.homemedkit.connectionController.RequestNew.getMedicine
import ru.application.homemedkit.connectionController.models.MainModel
import ru.application.homemedkit.databaseController.MedicineDatabase
import ru.application.homemedkit.helpers.ConstantsHelper.CATEGORY
import ru.application.homemedkit.viewModels.MedicineViewModel
import ru.application.homemedkit.viewModels.ResponseUiState


class RequestUpdate(
    val database: MedicineDatabase,
    private val viewModel: MedicineViewModel
) : Callback<MainModel> {

    override fun onResponse(call: Call<MainModel>, response: Response<MainModel>) {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                if (body.category == null) {
                    viewModel.responseUiState = ResponseUiState.Errors.WRONG_CODE_CATEGORY
                } else if (body.category.equals(CATEGORY)) {
                    if (body.codeFounded && body.checkResult) {
                        val medicine = getMedicine(body)

                        medicine.id = viewModel.id
                        medicine.comment = viewModel.comment
                        database.medicineDAO().update(medicine)

                        viewModel.responseUiState = ResponseUiState.Success

                    } else viewModel.responseUiState = ResponseUiState.Errors.CODE_NOT_FOUND
                } else viewModel.responseUiState = ResponseUiState.Errors.WRONG_CATEGORY
            } else viewModel.responseUiState = ResponseUiState.Errors.FETCH_ERROR
        }
    }

    override fun onFailure(call: Call<MainModel>, t: Throwable) {
        viewModel.responseUiState = ResponseUiState.Errors.NO_NETWORK
    }
}