CREATE TABLE `human` (
                         `id` int(11) NOT NULL AUTO_INCREMENT,
                         `name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
                         `age` int(11) DEFAULT NULL,
                         `likes` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=212 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;