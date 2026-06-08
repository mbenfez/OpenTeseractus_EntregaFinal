# OpenTeseractus

Aplicación Android para grupos de amigos que quieren llevar un seguimiento compartido de películas y series. Cada entrada se llama **teseracto**: un hilo de discusión centrado en un título concreto donde los miembros del grupo pueden valorarlo y chatear en tiempo real.

---

## Características principales

- Registro e inicio de sesión con Firebase Authentication
- Creación de grupos y sistema de invitaciones entre usuarios
- Búsqueda de películas y series a través de la API de TMDB
- Teseractos: hilos por título con valoración numérica y nota media del grupo
- Chat en tiempo real por teseracto (Firestore)
- Gestión de miembros y roles dentro del grupo
- Foto de perfil y foto de grupo almacenadas en Firebase Storage
- Notificaciones de nuevos mensajes
- Modo oscuro forzado en toda la aplicación

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend / Base de datos | Firebase Firestore |
| Autenticación | Firebase Authentication |
| Almacenamiento de imágenes | Firebase Storage |
| API de contenido | TMDB (The Movie Database) |
| Peticiones HTTP | Retrofit 2 + Gson |
| Carga de imágenes | Glide |
| UI | AndroidX · Material Design 3 |

---

## Requisitos

- **Android mínimo:** Android 7.0 Nougat (API 24)
- **Android recomendado:** Android 10 o superior
- **Android Studio:** Hedgehog o posterior
- **JDK:** 11
- Archivo `google-services.json` con una configuración de proyecto Firebase válida (debe colocarse en `app/`)
- Clave de API de TMDB configurada en el proyecto

---

## Estructura del proyecto

```
app/src/main/java/com/example/openteseractus/
├── adapters/        # Adaptadores RecyclerView (grupos, teseractos, mensajes, miembros...)
├── callbacks/       # Interfaces de callback para operaciones asíncronas de Firebase
├── modelos/         # Clases de datos (Grupo, Teseracto, Usuario, Mensaje, Valoracion...)
├── repositorios/    # Capa de acceso a Firestore (GrupoRepository, TeseractoRepository...)
├── servicios/       # Servicios en segundo plano (autenticación, notificaciones)
├── tmdb/            # Integración con la API de TMDB (cliente, modelos, repositorio)
└── ui/
    ├── auth/        # Login y registro
    ├── crear/       # Creación de grupos y teseractos
    ├── agregar/     # Búsqueda y adición de usuarios
    ├── fragments/   # Fragmentos reutilizables (cartelera, miembros)
    └── ventanas/    # Actividades principales (Home, Grupo, Teseracto, Chat, Invitaciones)
```

---

## Instalación

1. Clona el repositorio:
   ```bash
   git clone https://github.com/mbenfez/OpenTeseractus.git
   ```
2. Abre el proyecto en Android Studio.
3. Añade el archivo `google-services.json` de tu proyecto Firebase en la carpeta `app/`.
4. Configura tu clave de API de TMDB en el proyecto.
5. Sincroniza Gradle y ejecuta la aplicación en un emulador o dispositivo físico.

Además, existe la opción de descargar el apk desde github, en el último release de este repositorio.

---

## Autor

**Martín Benítez Fernández** — Trabajo de Fin de Grado (TFG)
