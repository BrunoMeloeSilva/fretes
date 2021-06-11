package br.com.zup.edu

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

        val calculaFrete = CalculaFreteResponse.newBuilder()
            .setCep(request!!.cep)
            .setValor(Random.nextDouble(0.0, 140.00))
            .build()

        logger.info("Frete calculado: $calculaFrete")

        response!!.onNext(calculaFrete)
        response.onCompleted()
    }
}