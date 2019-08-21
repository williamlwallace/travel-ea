package repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;
import models.Photo;
import models.Tag;
import models.User;
import org.junit.Before;
import org.junit.Test;
import util.objects.Pair;

public class PhotoRepositoryTest extends repository.RepositoryTest {

    private static PhotoRepository photoRepository;
    private static UserRepository userRepository;


    @Before
    public void runEvolutions() {
        applyEvolutions("test/photo/");
    }

    @Before
    public void instantiateRepository() {
        photoRepository = fakeApp.injector().instanceOf(PhotoRepository.class);
        userRepository = fakeApp.injector().instanceOf(UserRepository.class);
    }

    private Photo createPhoto() {
        Photo photo = new Photo();
        photo.usedForProfile = false;
        photo.thumbnailFilename = "test/thumbnail";
        photo.userId = 1L;
        photo.isPublic = false;
        photo.filename = "Idon'tlikethePlayFramework.jpeg";

        Tag tag = new Tag("Russia", 1L);

        photo.tags.add(tag);

        return photo;
    }

    @Test
    public void addPhoto() {
        Photo photo = createPhoto();
        assertEquals((Long) 4L, photoRepository.addPhoto(photo).join());
    }

    @Test(expected = CompletionException.class)
    public void addPhotoInvalidUser() {
        Photo photo = createPhoto();
        photo.userId = 99999L;
        photoRepository.addPhoto(photo).join();
    }

    @Test(expected = CompletionException.class)
    public void addPhotoInvalidGuid() {
        Photo photo = createPhoto();
        photo.guid = 1L;
        photoRepository.addPhoto(photo).join();
    }

    @Test
    public void addPhotos() {
        List<Photo> photosOriginal = photoRepository.getAllUserPhotos(1L).join();
        assertEquals(2, photosOriginal.size());

        User user = userRepository.findID(1L).join();

        Photo photo1 = createPhoto();
        Photo photo2 = createPhoto();
        Photo photo3 = createPhoto();

        List<Photo> newPhotos = new ArrayList<>();
        newPhotos.add(photo1);
        newPhotos.add(photo2);
        newPhotos.add(photo3);

        photoRepository.addPhotos(newPhotos, user).thenApplyAsync(result -> {
            List<Photo> photosNew = photoRepository.getAllUserPhotos(1L).join();

            assertEquals(5, photosNew.size());

            return null;
        });
    }

    @Test
    public void clearProfilePhoto() {
        Pair<String, String> filenames = photoRepository.clearProfilePhoto(1L).join();

        assertEquals("./public/storage/photos/test/test.jpeg", filenames.getKey());
        assertEquals("./public/storage/photos/test/thumbnails/test.jpeg", filenames.getValue());
    }

    @Test
    public void clearProfilePhotoNoProfilePhoto() {
        photoRepository.clearProfilePhoto(1L).join();
        Pair<String, String> filenames = photoRepository.clearProfilePhoto(1L).join();

        assertNull(filenames);
    }

    @Test
    public void getAllUserPhotos() {
        List<Photo> photos = photoRepository.getAllUserPhotos(1L).join();
        assertEquals(2, photos.size());
        assertEquals("./public/storage/photos/test/test2.jpeg",
            photos.get(0).filename); // With default assets path added on
        assertEquals(2, photos.get(0).tags.size());
        assertTrue(photos.get(0).tags.contains(new Tag("Russia")));
    }

    @Test
    public void getAllUserPhotosInvalidUser() {
        List<Photo> photos = photoRepository.getAllUserPhotos(99999L).join();
        assertEquals(0, photos.size());
    }

    @Test
    public void getAllPublicUserPhotos() {
        List<Photo> photos = photoRepository.getAllPublicUserPhotos(1L).join();
        assertEquals(1, photos.size());
        assertEquals("./public/storage/photos/test/test3.jpeg",
            photos.get(0).filename); // With default assets path added on
        assertEquals(0, photos.get(0).tags.size());

        for (Photo photo : photos) {
            assertEquals(true, photo.isPublic);
        }
    }

    @Test
    public void getAllPublicUserPhotosInvalidUser() {
        List<Photo> photos = photoRepository.getAllUserPhotos(99999L).join();
        assertEquals(0, photos.size());
    }

    @Test
    public void getUserProfilePicture() {
        Photo photo = photoRepository.getUserProfilePicture(1L).join();

        assertEquals((Long) 1L, photo.userId);
        assertEquals((Long) 1L, photo.guid);
        assertEquals(0, photo.tags.size());
        assertEquals(true, photo.usedForProfile);
        assertEquals(true, photo.isPublic);
    }

    @Test
    public void getUserProfilePictureInvalidUser() {
        Photo photo = photoRepository.getUserProfilePicture(99999L).join();

        assertNull(photo);
    }

    @Test
    public void deletePhoto() {
        Photo deletedPhoto = photoRepository.deletePhoto(1L).join();

        assertNotNull(deletedPhoto);
    }

    @Test
    public void deletePhotoInvalidId() {
        Photo deletedPhoto = photoRepository.deletePhoto(99999L).join();

        assertNull(deletedPhoto);
    }

//    @Test
//    public void appendAssetsUrl() {
//        Photo photo = createPhoto();
//        List<Photo> photos = new ArrayList<>();
//        photos.add(photo);
//
//        List<Photo> newPhotos = photoRepository.appendAssetsUrl(photos);
//        assertEquals(1, newPhotos.size());
//        assertEquals("../user_content/Idon'tlikethePlayFramework.jpeg", newPhotos.get(0).filename);
//        assertEquals("../user_content/test/thumbnail", newPhotos.get(0).thumbnailFilename);
//    }

    @Test
    public void getPhotoById() {
        Photo photo = photoRepository.getPhotoById(2L).join();
        assertEquals((Long) 2L, photo.guid);
        assertEquals((Long) 1L, photo.userId);
        assertEquals("./public/storage/photos/test/test2.jpeg", photo.filename);
        assertEquals("./public/storage/photos/test/thumbnails/test2.jpeg", photo.thumbnailFilename);
        assertEquals(2, photo.tags.size());
        assertTrue(photo.tags.contains(new Tag("Russia")));
    }

    @Test
    public void getPhotoByIdInvalidId() {
        Photo photo = photoRepository.getPhotoById(99999L).join();
        assertNull(photo);
    }

    @Test
    public void deletePhotoByFilename() {
        assertTrue(photoRepository.deletePhotoByFilename("./public/storage/photos/test/test3.jpeg")
            .join());
    }

    @Test
    public void deletePhotoByFilenameInvalidFilename() {
        assertFalse(
            photoRepository.deletePhotoByFilename("./public/storage/phoOOPSIEtos/test/test3.jpeg")
                .join());
        assertNotNull(photoRepository.getPhotoById(3L).join());
    }

    @Test
    public void updatePhoto() {
        Photo photo = photoRepository.getPhotoById(2L).join();
        photo.isPublic = true;
        photo.filename = "./new/filename";

        photoRepository.updatePhoto(photo).join();

        Photo updatedPhoto = photoRepository.getPhotoById(2L).join();
        assertTrue(updatedPhoto.isPublic);
        assertEquals("./new/filename", photo.filename);
        assertEquals((Long) 2L, photo.guid);
    }

    @Test(expected = CompletionException.class)
    public void updatePhotoInvalidId() {
        Photo photo = createPhoto();

        assertNull(photoRepository.updatePhoto(photo).join());
        assertNull(photoRepository.getPhotoById(4L).join());
    }

    @Test(expected = CompletionException.class)
    public void updatePhotoInvalidReferencedId() {
        Photo photo = createPhoto();
        photo.guid = 3L;
        photo.userId = 99999L;

        photoRepository.updatePhoto(photo).join();
    }
}
