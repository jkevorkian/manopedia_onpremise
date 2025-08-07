Sobre levantarlo, vas a necesitar una base mongodb. Nosotros lo levantamos con MongoDB Atlas (el mongodb en la nube). Eso no debería ser complicado

Después, del backend, está este documento que hice en su momento: "how_to_deploy_backend.md"

Primero armas la imagen docker en tu local, la subis al Google Cloud y ahí levantas un contenedor Docker a partir de la imagen que subiste

En el backend capaz que tenés que configurar algunas variables de entorno (por ejemplo, la url de mongodb). Revisé en mi compu y no encontré esa config

Para el frontend, fijate en el repo la branch "flutter". Ahí sí hay un .env de prod y dev para la url del backend, por ejemplo

También hay un script update_and_run_backend.ps1. Eso compila el backend y crea la imagen docker, no sé por qué lo puse ahí xd

Y el front se compila con flutter build apk --release, o eso creo. Está en el readme

Otra alternativa a eso sería mandar el backend y la base de mongodb alv y poner una base de datos en Flutter. Con eso, sólo se necesitaría instalar la app y no dependería de tener un server levantado

Pero, hay que tocar el código