package br.com.breno.animallovers.service

import br.com.breno.animallovers.constants.NumberConstants

class FotoService {

    companion object  {
        fun tratarQualidadeFoto(bitmapByteCount : Int) : Int {

            var quality : Int

            when {
                bitmapByteCount > NumberConstants.TEN_MILLION.value -> {
                    quality = 12
                }
                bitmapByteCount > NumberConstants.ONE_MILLION.value && bitmapByteCount < NumberConstants.TEN_MILLION.value -> {
                    quality = 40
                }
                bitmapByteCount > NumberConstants.ONE_HUNDRED_THOUSAND.value && bitmapByteCount < NumberConstants.ONE_MILLION.value -> {
                    quality = 70
                }
                bitmapByteCount > NumberConstants.TEN_THOUSAND.value && bitmapByteCount < NumberConstants.ONE_HUNDRED_THOUSAND.value -> {
                    quality = 82
                }
                else -> {
                    quality = 95
                }
            }
            return quality
        }
        }

}