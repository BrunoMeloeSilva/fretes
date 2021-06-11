package br.com.zup.edu

import com.google.protobuf.Any
import com.google.rpc.Code
import com.google.rpc.StatusProto
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FreteGrpcServer: FretesServiceGrpc.FretesServiceImplBase() {

    private val logger = LoggerFactory.getLogger(FreteGrpcServer::class.java)

    override fun calculaFrete(request: CalculaFreteRequest?,
                              response: StreamObserver<CalculaFreteResponse>?) {
        logger.info("Calculando frete para request: $request")
        //Validação de dados
        val cep = request?.cep
        if (cep == null || cep.isBlank()){
            //throw IllegalArgumentException("cep deve ser informado")
            //val error = StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("cep deve ser informado"))
            val error = Status.INVALID_ARGUMENT
                .withDescription("cep deve ser informado")
                .asRuntimeException()

            response?.onError(error)
        }

        if (!cep!!.matches("[0-9]{5}-[0-9]{3}".toRegex())){
            val error = Status.INVALID_ARGUMENT
                .withDescription("cep inválido")
                .augmentDescription("formato esperado deve ser 99999-999")
                .asRuntimeException()

            response?.onError(error)
        }
        // Forçando um erro de segurança
        if(cep.endsWith("333")){
            val errorDetails = ErrorDetails.newBuilder().setCode(401).setMessage("Token expirado").build()
            val statusProto = com.google.rpc.Status.newBuilder()
                .setCode(Code.PERMISSION_DENIED.number)
                .setMessage("Usuario não pode acessar esse recurso")
                .addDetails(Any.pack(errorDetails))
                .build()
            val erro = io.grpc.protobuf.StatusProto.toStatusRuntimeException(statusProto)
            response?.onError(erro)
        }

        // Forçando outro tipo de erro inesperado.
        var valor = 0.0
        try {
            valor = Random.nextDouble(0.0, 140.00)
            if(valor > 100.0){
                throw IllegalStateException("Erro interno inesperado.")
            }
        } catch (e: Exception){
            response?.onError(Status.INTERNAL
                .withDescription(e.message)
                .withCause(e)
                .asRuntimeException())
        }
        // Fim
        val calculaFrete = CalculaFreteResponse.newBuilder()
            .setCep(request.cep)
            .setValor(valor)
            .build()

        logger.info("Frete calculado: $calculaFrete")

        response!!.onNext(calculaFrete)
        response.onCompleted()
    }
}