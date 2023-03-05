package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User(name, mobile);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Album album = new Album(title);
        albums.add(album);
        for(Artist artist: artists) {
            if(artist.equals(artistName)) {
                List<Album> currentAlbum = artistAlbumMap.get(artist);
                currentAlbum.add(album);
                artistAlbumMap.put(artist, currentAlbum);
                return album;
            }
        }
        Artist artist = new Artist(artistName);
        artists.add(artist);
        List<Album> currentAlbum = new ArrayList<>();
        currentAlbum.add(album);
        artistAlbumMap.put(artist, currentAlbum);
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        for(Album album: albums) {
            if(album.equals(albumName)) {
                Song song = new Song(title, length);
                songs.add(song);
                List<Song> songList = albumSongMap.get(album);
                songList.add(song);
                albumSongMap.put(album, songList);
                return song;
            }
        }
        throw new RuntimeException("Album does not exist");
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        for(User user: users) {
            if(user.getMobile().equals(mobile)) {
                Playlist playlist = new Playlist(title);
                playlists.add(playlist);
                List<Song> songList = new ArrayList<>();
                for(Song song: songs) {
                    if(song.getLength() == length) {
                        songList.add(song);
                    }
                }
                playlistSongMap.put(playlist, songList);
                List<User> userList = new ArrayList<>();
                userList.add(user);
                playlistListenerMap.put(playlist, userList);
                List<Playlist> playlistList = userPlaylistMap.get(user);
                playlistList.add(playlist);
                userPlaylistMap.put(user, playlistList);
                creatorPlaylistMap.put(user, playlist);
                return playlist;
            }
        }
        throw new RuntimeException("User does not exist");
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        for(User user: users) {
            if(user.getMobile().equals(mobile)) {
                Playlist playlist = new Playlist(title);
                List<Song> songList = new ArrayList<>();
                for(Song song:songs) {
                    for(String givenSong: songTitles) {
                        if(song.getTitle().equals(givenSong)) {
                            songList.add(song);
                        }
                    }
                }
                playlistSongMap.put(playlist, songList);
                playlists.add(playlist);
                creatorPlaylistMap.put(user, playlist);
                List<User> userList = playlistListenerMap.get(playlist);
                userList.add(user);
                playlistListenerMap.put(playlist, userList);
                List<Playlist> playlistList = userPlaylistMap.get(user);
                playlistList.add(playlist);
                userPlaylistMap.put(user, playlistList);
            }
        }
        throw new RuntimeException("User does not exist");
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        for(User user: users) {
            if(user.getMobile().equals(mobile)) {
                for(Playlist playlist: playlists) {
                    if(playlist.getTitle().equals(playlistTitle)) {
                        if(isValid(user, playlist)) {
                            return playlist;
                        }
                        List<User> listnerList = playlistListenerMap.get(playlist);
                        listnerList.add(user);
                        playlistListenerMap.put(playlist, listnerList);
                        List<Playlist> playlistList = userPlaylistMap.get(user);
                        playlistList.add(playlist);
                        userPlaylistMap.put(user, playlistList);
                        return playlist;
                    }
                }
                throw new RuntimeException("Playlist does not exist");
            }
        }
        throw new RuntimeException("User does not exist");
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        for(User user: users) {
            if(user.getMobile().equals(mobile)) {
                for(Song song: songs) {
                    if(song.getTitle().equals(songTitle)) {
                        if(!validLike(song, user)) {
                            return song;
                        }
                        List<User> userList = songLikeMap.get(song);
                        userList.add(user);
                        songLikeMap.put(song, userList);
                        song.setLikes(song.getLikes()+1);
                        for(Album album: albumSongMap.keySet()) {
                            List<Song> songList = albumSongMap.get(album);
                            for(Song song1:songList) {
                                if(song1.equals(song)) {
                                    for(Artist artist: artistAlbumMap.keySet()) {
                                        List<Album> albumList = artistAlbumMap.get(artist);
                                        for(Album album1: albumList) {
                                            if(album1.equals(album)) {
                                                artist.setLikes(artist.getLikes()+1);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        return song;
                    }
                }
                throw new RuntimeException("Song does not exist");
            }
        }
        throw new RuntimeException("User does not exist");
    }

    public String mostPopularArtist() {
        int mostLikes = 0;
        String mostPopularArtist = "";
        for(Artist artist: artists) {
            if(artist.getLikes() > mostLikes) {
                mostLikes = artist.getLikes();
                mostPopularArtist = artist.getName();
            }
        }
        return mostPopularArtist;
    }

    public String mostPopularSong() {
        int mostLikes = 0;
        String mostPopularSong = "";
        for(Song song: songs) {
            if(song.getLikes() > mostLikes) {
                mostLikes = song.getLikes();
                mostPopularSong = song.getTitle();
            }
        }
        return mostPopularSong;
    }
    public boolean isValid(User user, Playlist playlist) {
        if(creatorPlaylistMap.get(user).equals(playlist)) return true;
        for(User user1: playlistListenerMap.get(playlist)) {
            if(user1.equals(user)) {
                return true;
            }
        }
        return false;
    }
    public boolean validLike(Song song, User user) {
        for(User user1: songLikeMap.get(song)) {
            if(user1.equals(user)) return false;
        }
        return true;
    }
}
