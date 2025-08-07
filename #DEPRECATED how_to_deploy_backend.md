Video tutorial:
https://www.youtube.com/watch?v=cw34KMPSt4k&ab_channel=BeyondFireship

Otro tutorial:
https://medium.com/@mohamed.mywork/run-docker-for-free-with-google-cloud-shell-c3d66d905b5d

Subir la imagen docker al registry de google cloud

Dirigirse a la consola de google cloud:  
https://console.cloud.google.com/welcome/new?hl=es

Acceder a google artifact registry:
https://console.cloud.google.com/artifacts/browse/dark-form-435501-k1?project=dark-form-435501-k1

Ir al repositorio:
https://console.cloud.google.com/artifacts/docker/dark-form-435501-k1/southamerica-west1/manopedia-repository?project=dark-form-435501-k1

Desde esa página, seguir las instrucciones de configuración (se deben ejecutar en la consola de google cloud de la misma página)

Instalar Google Cloud SDK y seguir los pasos para iniciarlo con el proyecto correspondiente al repositorio.

Una vez configurado, en el CLI de Google Cloud SDK, ejecutar (según la región):

gcloud auth configure-docker southamerica-west1-docker.pkg.dev

Luego, ejecutar en otra consola (según la región y reemplazando <id_imagen_docker>):

docker tag <id_imagen_docker> southamerica-west1-docker.pkg.dev/dark-form-435501-k1/manopedia-repository/manopedia-backend

docker push southamerica-west1-docker.pkg.dev/dark-form-435501-k1/manopedia-repository/manopedia-backend

Ejecutar contenedor docker en google cloud

Ir a la sección “Cloud Run” de la página de google cloud:
	https://console.cloud.google.com/run?referrer=search&authuser=1&project=dark-form-435501-k1

Seleccionar “Implementar contenedor” -> “Servicio”

Seleccionar la imagen subida al artifact registry y configurar el despliegue del contenedor.