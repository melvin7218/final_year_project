-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Aug 03, 2025 at 08:35 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `finalyearproject`
--

-- --------------------------------------------------------

--
-- Table structure for table `exercise`
--

CREATE TABLE `exercise` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `MET` double(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `exercise`
--

INSERT INTO `exercise` (`id`, `name`, `MET`) VALUES
(1, 'Walking (slow, 2.0 mph)', 2.00),
(2, 'Walking (moderate, 3.0 mph)', 3.30),
(3, 'Walking (brisk, 4.0 mph)', 5.00),
(4, 'Running (5 mph / 8 km/h)', 8.30),
(5, 'Running (6 mph / 9.7 km/h)', 9.80),
(6, 'Running (7.5 mph / 12 km/h)', 11.50),
(7, 'Running (10 mph / 16 km/h)', 16.00),
(8, 'Cycling (light effort)', 4.00),
(9, 'Cycling (moderate effort)', 6.80),
(10, 'Cycling (vigorous effort)', 10.00),
(11, 'Swimming (slow pace)', 6.00),
(12, 'Swimming (moderate pace)', 8.00),
(13, 'Swimming (fast pace)', 10.00),
(14, 'Jumping rope (slow)', 8.80),
(15, 'Jumping rope (fast)', 12.30),
(16, 'Bowling', 3.00),
(17, 'Yoga (Hatha)', 2.50),
(18, 'Yoga (power)', 4.00),
(19, 'Dancing (slow)', 3.50),
(20, 'Dancing (aerobic)', 7.30),
(21, 'Hiking (general)', 6.00),
(22, 'Strength training (light)', 3.50),
(23, 'Strength training (heavy)', 6.00),
(24, 'Basketball (shooting hoops)', 4.50),
(25, 'Basketball (game)', 8.00),
(26, 'Soccer (casual)', 7.00),
(27, 'Soccer (competitive)', 10.00),
(28, 'Tennis (doubles)', 5.00),
(29, 'Tennis (singles)', 8.00),
(30, 'Badminton (casual)', 4.50),
(31, 'Badminton (competitive)', 7.00),
(32, 'House cleaning (light)', 2.50),
(33, 'House cleaning (vigorous)', 3.80);

-- --------------------------------------------------------

--
-- Table structure for table `favorite_recipes`
--

CREATE TABLE `favorite_recipes` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `recipe_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `favorite_recipes`
--

INSERT INTO `favorite_recipes` (`id`, `user_id`, `recipe_id`, `created_at`) VALUES
(3, 2, 19, '2025-07-21 06:26:30'),
(4, 2, 20, '2025-07-21 11:09:38'),
(5, 2, 1, '2025-07-21 11:09:42');

-- --------------------------------------------------------

--
-- Table structure for table `grocery_lists`
--

CREATE TABLE `grocery_lists` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `recipe_id` int(11) NOT NULL,
  `is_user_recipe` tinyint(1) NOT NULL DEFAULT 1,
  `ingredient_name` varchar(255) NOT NULL,
  `amount` varchar(50) DEFAULT NULL,
  `added_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `purchased` tinyint(1) DEFAULT 0,
  `week` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `grocery_lists`
--

INSERT INTO `grocery_lists` (`id`, `user_id`, `recipe_id`, `is_user_recipe`, `ingredient_name`, `amount`, `added_at`, `purchased`, `week`) VALUES
(38, 6, 53085, 1, 'Buns', '2', '2025-04-14 05:58:14', 0, ''),
(39, 6, 53050, 1, 'Cumin', '1 1/2', '2025-04-14 05:58:50', 0, ''),
(53, 1, 10, 1, 'milk', '980 ml', '2025-05-11 06:22:53', 0, ''),
(54, 1, 10, 1, 'olive oil', '59.2 ml', '2025-05-11 06:22:53', 0, ''),
(59, 2, 634471, 1, 'Spaghetti', '400 g', '2025-05-11 08:55:34', 1, ''),
(60, 2, 634471, 1, 'Bacon', '200 g', '2025-05-11 08:55:34', 1, ''),
(61, 2, 634471, 1, 'Parmesan', '100 g', '2025-05-11 08:55:34', 1, ''),
(62, 2, 634471, 1, 'Egg', '2 pieces', '2025-05-11 09:15:55', 1, ''),
(64, 2, 634473, 1, 'carrots', '3.0 medium', '2025-05-11 16:05:15', 1, ''),
(66, 2, 634473, 1, 'chicken breast', '2.0 cups', '2025-05-11 16:05:15', 1, ''),
(68, 2, 634473, 1, 'garlic', '6.0 cloves', '2025-05-11 16:05:15', 1, ''),
(71, 2, 634473, 1, 'canned tomatoes', '28.0 ounce', '2025-05-11 16:05:15', 1, ''),
(76, 24, 663078, 1, 'sugar', '1.5 tablespoons', '2025-05-12 16:35:51', 1, ''),
(77, 2, 634473, 1, 'carrots', '3.0 medium', '2025-05-13 02:46:22', 1, ''),
(79, 2, 634473, 1, 'chicken breast', '2.0 cups', '2025-05-13 02:46:22', 1, ''),
(81, 2, 0, 1, 'salt', 'for boiling water', '2025-07-07 06:46:03', 0, ''),
(82, 2, 0, 1, 'choy sum', '100 g', '2025-07-07 06:46:03', 0, ''),
(83, 2, 0, 1, 'water', '130.00', '2025-07-22 04:41:28', 1, ''),
(84, 2, 0, 1, 'vegetable oil', '2.00', '2025-07-22 04:41:28', 1, '');

-- --------------------------------------------------------

--
-- Table structure for table `ingredient_conversions`
--

CREATE TABLE `ingredient_conversions` (
  `ingredient_name` varchar(255) NOT NULL,
  `unit` varchar(50) NOT NULL,
  `grams_per_unit` decimal(10,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ingredient_nutrition`
--

CREATE TABLE `ingredient_nutrition` (
  `ingredient_id` int(11) NOT NULL,
  `ingredient_name` varchar(100) NOT NULL,
  `calories` float NOT NULL,
  `protein` float NOT NULL,
  `carbohydrates` float NOT NULL,
  `fat` float NOT NULL,
  `fiber` float DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `ingredient_nutrition`
--

INSERT INTO `ingredient_nutrition` (`ingredient_id`, `ingredient_name`, `calories`, `protein`, `carbohydrates`, `fat`, `fiber`) VALUES
(2, 'Chinese Broccoli', 21, 2.8, 3.1, 0.3, 1.8),
(3, 'Choy Sum', 19, 2.3, 2.5, 0.2, 1.6),
(4, 'Napa Cabbage', 16, 1, 3.2, 0.2, 1.2),
(5, 'Kangkong', 19, 2.6, 3.1, 0.2, 2),
(6, 'Long Bean', 47, 2.8, 8.4, 0.4, 2.7),
(7, 'Bitter Melon', 17, 1, 3.7, 0.2, 2.8),
(8, 'Okra', 33, 2, 7.5, 0.2, 3.2),
(9, 'Daikon Radish', 18, 0.6, 4.1, 0.1, 1.6),
(10, 'Lotus Root', 74, 2.6, 17.2, 0.1, 4.9),
(11, 'Taro', 112, 1.5, 26.5, 0.2, 4.1),
(12, 'Yam', 118, 1.5, 27.9, 0.2, 4.2),
(13, 'Sweet Potato', 86, 1.6, 20.1, 0.1, 3),
(14, 'Spring Onion', 32, 1.8, 7.3, 0.2, 2.6),
(15, 'Garlic', 149, 6.4, 33.1, 0.5, 2.1),
(16, 'Ginger', 80, 1.8, 17.8, 0.8, 2),
(17, 'Shallot', 72, 2.5, 16.8, 0.1, 2.2),
(18, 'Cilantro', 23, 2.1, 3.7, 0.5, 1.8),
(19, 'Thai Basil', 23, 3.2, 2.7, 0.6, 1.5),
(20, 'Mint', 44, 3.3, 8.4, 0.7, 2),
(21, 'Bok Choy Var2', 13, 1.5, 2.2, 0.2, 1),
(22, 'Chinese Broccoli Var2', 21, 2.8, 3.1, 0.3, 1.8),
(23, 'Choy Sum Var2', 19, 2.3, 2.5, 0.2, 1.6),
(24, 'Napa Cabbage Var2', 16, 1, 3.2, 0.2, 1.2),
(25, 'Kangkong Var2', 19, 2.6, 3.1, 0.2, 2),
(26, 'Long Bean Var2', 47, 2.8, 8.4, 0.4, 2.7),
(27, 'Bitter Melon Var2', 17, 1, 3.7, 0.2, 2.8),
(28, 'Okra Var2', 33, 2, 7.5, 0.2, 3.2),
(29, 'Daikon Radish Var2', 18, 0.6, 4.1, 0.1, 1.6),
(30, 'Lotus Root Var2', 74, 2.6, 17.2, 0.1, 4.9),
(31, 'Taro Var2', 112, 1.5, 26.5, 0.2, 4.1),
(32, 'Yam Var2', 118, 1.5, 27.9, 0.2, 4.2),
(33, 'Sweet Potato Var2', 86, 1.6, 20.1, 0.1, 3),
(34, 'Spring Onion Var2', 32, 1.8, 7.3, 0.2, 2.6),
(35, 'Garlic Var2', 149, 6.4, 33.1, 0.5, 2.1),
(36, 'Ginger Var2', 80, 1.8, 17.8, 0.8, 2),
(37, 'Shallot Var2', 72, 2.5, 16.8, 0.1, 2.2),
(38, 'Cilantro Var2', 23, 2.1, 3.7, 0.5, 1.8),
(39, 'Thai Basil Var2', 23, 3.2, 2.7, 0.6, 1.5),
(40, 'Mint Var2', 44, 3.3, 8.4, 0.7, 2),
(41, 'Bok Choy Var3', 13, 1.5, 2.2, 0.2, 1),
(42, 'Chinese Broccoli Var3', 21, 2.8, 3.1, 0.3, 1.8),
(43, 'Choy Sum Var3', 19, 2.3, 2.5, 0.2, 1.6),
(44, 'Napa Cabbage Var3', 16, 1, 3.2, 0.2, 1.2),
(45, 'Kangkong Var3', 19, 2.6, 3.1, 0.2, 2),
(46, 'Long Bean Var3', 47, 2.8, 8.4, 0.4, 2.7),
(47, 'Bitter Melon Var3', 17, 1, 3.7, 0.2, 2.8),
(48, 'Okra Var3', 33, 2, 7.5, 0.2, 3.2),
(49, 'Daikon Radish Var3', 18, 0.6, 4.1, 0.1, 1.6),
(50, 'Lotus Root Var3', 74, 2.6, 17.2, 0.1, 4.9),
(51, 'Taro Var3', 112, 1.5, 26.5, 0.2, 4.1),
(52, 'Yam Var3', 118, 1.5, 27.9, 0.2, 4.2),
(53, 'Sweet Potato Var3', 86, 1.6, 20.1, 0.1, 3),
(54, 'Spring Onion Var3', 32, 1.8, 7.3, 0.2, 2.6),
(55, 'Garlic Var3', 149, 6.4, 33.1, 0.5, 2.1),
(56, 'Ginger Var3', 80, 1.8, 17.8, 0.8, 2),
(57, 'Shallot Var3', 72, 2.5, 16.8, 0.1, 2.2),
(58, 'Cilantro Var3', 23, 2.1, 3.7, 0.5, 1.8),
(59, 'Thai Basil Var3', 23, 3.2, 2.7, 0.6, 1.5),
(60, 'Mint Var3', 44, 3.3, 8.4, 0.7, 2),
(61, 'Bok Choy Var4', 13, 1.5, 2.2, 0.2, 1),
(62, 'Chinese Broccoli Var4', 21, 2.8, 3.1, 0.3, 1.8),
(63, 'Choy Sum Var4', 19, 2.3, 2.5, 0.2, 1.6),
(64, 'Napa Cabbage Var4', 16, 1, 3.2, 0.2, 1.2),
(65, 'Kangkong Var4', 19, 2.6, 3.1, 0.2, 2),
(66, 'Long Bean Var4', 47, 2.8, 8.4, 0.4, 2.7),
(67, 'Bitter Melon Var4', 17, 1, 3.7, 0.2, 2.8),
(68, 'Okra Var4', 33, 2, 7.5, 0.2, 3.2),
(69, 'Daikon Radish Var4', 18, 0.6, 4.1, 0.1, 1.6),
(70, 'Lotus Root Var4', 74, 2.6, 17.2, 0.1, 4.9),
(71, 'Taro Var4', 112, 1.5, 26.5, 0.2, 4.1),
(72, 'Yam Var4', 118, 1.5, 27.9, 0.2, 4.2),
(73, 'Sweet Potato Var4', 86, 1.6, 20.1, 0.1, 3),
(74, 'Spring Onion Var4', 32, 1.8, 7.3, 0.2, 2.6),
(75, 'Garlic Var4', 149, 6.4, 33.1, 0.5, 2.1),
(76, 'Ginger Var4', 80, 1.8, 17.8, 0.8, 2),
(77, 'Shallot Var4', 72, 2.5, 16.8, 0.1, 2.2),
(78, 'Cilantro Var4', 23, 2.1, 3.7, 0.5, 1.8),
(79, 'Thai Basil Var4', 23, 3.2, 2.7, 0.6, 1.5),
(80, 'Mint Var4', 44, 3.3, 8.4, 0.7, 2),
(81, 'Bok Choy Var5', 13, 1.5, 2.2, 0.2, 1),
(82, 'Chinese Broccoli Var5', 21, 2.8, 3.1, 0.3, 1.8),
(83, 'Choy Sum Var5', 19, 2.3, 2.5, 0.2, 1.6),
(84, 'Napa Cabbage Var5', 16, 1, 3.2, 0.2, 1.2),
(85, 'Kangkong Var5', 19, 2.6, 3.1, 0.2, 2),
(86, 'Long Bean Var5', 47, 2.8, 8.4, 0.4, 2.7),
(87, 'Bitter Melon Var5', 17, 1, 3.7, 0.2, 2.8),
(88, 'Okra Var5', 33, 2, 7.5, 0.2, 3.2),
(89, 'Daikon Radish Var5', 18, 0.6, 4.1, 0.1, 1.6),
(90, 'Lotus Root Var5', 74, 2.6, 17.2, 0.1, 4.9),
(91, 'Taro Var5', 112, 1.5, 26.5, 0.2, 4.1),
(92, 'Yam Var5', 118, 1.5, 27.9, 0.2, 4.2),
(93, 'Sweet Potato Var5', 86, 1.6, 20.1, 0.1, 3),
(94, 'Spring Onion Var5', 32, 1.8, 7.3, 0.2, 2.6),
(95, 'Garlic Var5', 149, 6.4, 33.1, 0.5, 2.1),
(96, 'Ginger Var5', 80, 1.8, 17.8, 0.8, 2),
(97, 'Shallot Var5', 72, 2.5, 16.8, 0.1, 2.2),
(98, 'Cilantro Var5', 23, 2.1, 3.7, 0.5, 1.8),
(99, 'Thai Basil Var5', 23, 3.2, 2.7, 0.6, 1.5),
(100, 'Mint Var5', 44, 3.3, 8.4, 0.7, 2),
(101, 'Bok Choy Var6', 13, 1.5, 2.2, 0.2, 1),
(102, 'Chinese Broccoli Var6', 21, 2.8, 3.1, 0.3, 1.8),
(103, 'Choy Sum Var6', 19, 2.3, 2.5, 0.2, 1.6),
(104, 'Napa Cabbage Var6', 16, 1, 3.2, 0.2, 1.2),
(105, 'Kangkong Var6', 19, 2.6, 3.1, 0.2, 2),
(106, 'Long Bean Var6', 47, 2.8, 8.4, 0.4, 2.7),
(107, 'Bitter Melon Var6', 17, 1, 3.7, 0.2, 2.8),
(108, 'Okra Var6', 33, 2, 7.5, 0.2, 3.2),
(109, 'Daikon Radish Var6', 18, 0.6, 4.1, 0.1, 1.6),
(110, 'Lotus Root Var6', 74, 2.6, 17.2, 0.1, 4.9),
(111, 'Taro Var6', 112, 1.5, 26.5, 0.2, 4.1),
(112, 'Yam Var6', 118, 1.5, 27.9, 0.2, 4.2),
(113, 'Sweet Potato Var6', 86, 1.6, 20.1, 0.1, 3),
(114, 'Spring Onion Var6', 32, 1.8, 7.3, 0.2, 2.6),
(115, 'Garlic Var6', 149, 6.4, 33.1, 0.5, 2.1),
(116, 'Ginger Var6', 80, 1.8, 17.8, 0.8, 2),
(117, 'Shallot Var6', 72, 2.5, 16.8, 0.1, 2.2),
(118, 'Cilantro Var6', 23, 2.1, 3.7, 0.5, 1.8),
(119, 'Thai Basil Var6', 23, 3.2, 2.7, 0.6, 1.5),
(120, 'Mint Var6', 44, 3.3, 8.4, 0.7, 2),
(121, 'Bok Choy Var7', 13, 1.5, 2.2, 0.2, 1),
(122, 'Chinese Broccoli Var7', 21, 2.8, 3.1, 0.3, 1.8),
(123, 'Choy Sum Var7', 19, 2.3, 2.5, 0.2, 1.6),
(124, 'Napa Cabbage Var7', 16, 1, 3.2, 0.2, 1.2),
(125, 'Kangkong Var7', 19, 2.6, 3.1, 0.2, 2),
(126, 'Long Bean Var7', 47, 2.8, 8.4, 0.4, 2.7),
(127, 'Bitter Melon Var7', 17, 1, 3.7, 0.2, 2.8),
(128, 'Okra Var7', 33, 2, 7.5, 0.2, 3.2),
(129, 'Daikon Radish Var7', 18, 0.6, 4.1, 0.1, 1.6),
(130, 'Lotus Root Var7', 74, 2.6, 17.2, 0.1, 4.9),
(131, 'Taro Var7', 112, 1.5, 26.5, 0.2, 4.1),
(132, 'Yam Var7', 118, 1.5, 27.9, 0.2, 4.2),
(133, 'Sweet Potato Var7', 86, 1.6, 20.1, 0.1, 3),
(134, 'Spring Onion Var7', 32, 1.8, 7.3, 0.2, 2.6),
(135, 'Garlic Var7', 149, 6.4, 33.1, 0.5, 2.1),
(136, 'Ginger Var7', 80, 1.8, 17.8, 0.8, 2),
(137, 'Shallot Var7', 72, 2.5, 16.8, 0.1, 2.2),
(138, 'Cilantro Var7', 23, 2.1, 3.7, 0.5, 1.8),
(139, 'Thai Basil Var7', 23, 3.2, 2.7, 0.6, 1.5),
(140, 'Mint Var7', 44, 3.3, 8.4, 0.7, 2),
(141, 'Bok Choy Var8', 13, 1.5, 2.2, 0.2, 1),
(142, 'Chinese Broccoli Var8', 21, 2.8, 3.1, 0.3, 1.8),
(143, 'Choy Sum Var8', 19, 2.3, 2.5, 0.2, 1.6),
(144, 'Napa Cabbage Var8', 16, 1, 3.2, 0.2, 1.2),
(145, 'Kangkong Var8', 19, 2.6, 3.1, 0.2, 2),
(146, 'Long Bean Var8', 47, 2.8, 8.4, 0.4, 2.7),
(147, 'Bitter Melon Var8', 17, 1, 3.7, 0.2, 2.8),
(148, 'Okra Var8', 33, 2, 7.5, 0.2, 3.2),
(149, 'Daikon Radish Var8', 18, 0.6, 4.1, 0.1, 1.6),
(150, 'Lotus Root Var8', 74, 2.6, 17.2, 0.1, 4.9),
(151, 'Taro Var8', 112, 1.5, 26.5, 0.2, 4.1),
(152, 'Yam Var8', 118, 1.5, 27.9, 0.2, 4.2),
(153, 'Sweet Potato Var8', 86, 1.6, 20.1, 0.1, 3),
(154, 'Spring Onion Var8', 32, 1.8, 7.3, 0.2, 2.6),
(155, 'Garlic Var8', 149, 6.4, 33.1, 0.5, 2.1),
(156, 'Ginger Var8', 80, 1.8, 17.8, 0.8, 2),
(157, 'Shallot Var8', 72, 2.5, 16.8, 0.1, 2.2),
(158, 'Cilantro Var8', 23, 2.1, 3.7, 0.5, 1.8),
(159, 'Thai Basil Var8', 23, 3.2, 2.7, 0.6, 1.5),
(160, 'Mint Var8', 44, 3.3, 8.4, 0.7, 2),
(161, 'Bok Choy Var9', 13, 1.5, 2.2, 0.2, 1),
(162, 'Chinese Broccoli Var9', 21, 2.8, 3.1, 0.3, 1.8),
(163, 'Choy Sum Var9', 19, 2.3, 2.5, 0.2, 1.6),
(164, 'Napa Cabbage Var9', 16, 1, 3.2, 0.2, 1.2),
(165, 'Kangkong Var9', 19, 2.6, 3.1, 0.2, 2),
(166, 'Long Bean Var9', 47, 2.8, 8.4, 0.4, 2.7),
(167, 'Bitter Melon Var9', 17, 1, 3.7, 0.2, 2.8),
(168, 'Okra Var9', 33, 2, 7.5, 0.2, 3.2),
(169, 'Daikon Radish Var9', 18, 0.6, 4.1, 0.1, 1.6),
(170, 'Lotus Root Var9', 74, 2.6, 17.2, 0.1, 4.9),
(171, 'Taro Var9', 112, 1.5, 26.5, 0.2, 4.1),
(172, 'Yam Var9', 118, 1.5, 27.9, 0.2, 4.2),
(173, 'Sweet Potato Var9', 86, 1.6, 20.1, 0.1, 3),
(174, 'Spring Onion Var9', 32, 1.8, 7.3, 0.2, 2.6),
(175, 'Garlic Var9', 149, 6.4, 33.1, 0.5, 2.1),
(176, 'Ginger Var9', 80, 1.8, 17.8, 0.8, 2),
(177, 'Shallot Var9', 72, 2.5, 16.8, 0.1, 2.2),
(178, 'Cilantro Var9', 23, 2.1, 3.7, 0.5, 1.8),
(179, 'Thai Basil Var9', 23, 3.2, 2.7, 0.6, 1.5),
(180, 'Mint Var9', 44, 3.3, 8.4, 0.7, 2),
(181, 'Bok Choy Var10', 13, 1.5, 2.2, 0.2, 1),
(182, 'Chinese Broccoli Var10', 21, 2.8, 3.1, 0.3, 1.8),
(183, 'Choy Sum Var10', 19, 2.3, 2.5, 0.2, 1.6),
(184, 'Napa Cabbage Var10', 16, 1, 3.2, 0.2, 1.2),
(185, 'Kangkong Var10', 19, 2.6, 3.1, 0.2, 2),
(186, 'Long Bean Var10', 47, 2.8, 8.4, 0.4, 2.7),
(187, 'Bitter Melon Var10', 17, 1, 3.7, 0.2, 2.8),
(188, 'Okra Var10', 33, 2, 7.5, 0.2, 3.2),
(189, 'Daikon Radish Var10', 18, 0.6, 4.1, 0.1, 1.6),
(190, 'Lotus Root Var10', 74, 2.6, 17.2, 0.1, 4.9),
(191, 'Taro Var10', 112, 1.5, 26.5, 0.2, 4.1),
(192, 'Yam Var10', 118, 1.5, 27.9, 0.2, 4.2),
(193, 'Sweet Potato Var10', 86, 1.6, 20.1, 0.1, 3),
(194, 'Spring Onion Var10', 32, 1.8, 7.3, 0.2, 2.6),
(195, 'Garlic Var10', 149, 6.4, 33.1, 0.5, 2.1),
(196, 'Ginger Var10', 80, 1.8, 17.8, 0.8, 2),
(197, 'Shallot Var10', 72, 2.5, 16.8, 0.1, 2.2),
(198, 'Cilantro Var10', 23, 2.1, 3.7, 0.5, 1.8),
(199, 'Thai Basil Var10', 23, 3.2, 2.7, 0.6, 1.5),
(200, 'Mint Var10', 44, 3.3, 8.4, 0.7, 2),
(201, 'Spaghetti (cooked)', 158, 5.8, 30.9, 0.9, 1.8),
(202, 'Egg yolk', 322, 15.9, 3.6, 26.5, 0),
(203, 'Whole egg', 143, 12.6, 0.7, 9.5, 0),
(204, 'Parmesan cheese', 431, 38.5, 4.1, 29.7, 0),
(205, 'Bacon (cooked)', 541, 37, 1.4, 42, 0),
(206, 'Garlic (raw)', 149, 6.4, 33.1, 0.5, 2.1),
(207, 'Olive oil', 884, 0, 0, 100, 0),
(208, 'Black pepper', 251, 10.4, 63.9, 3.3, 25.3),
(209, 'Salt', 0, 0, 0, 0, 0),
(210, 'Cooked Rice', 130, 2.7, 28, 0.3, 0.4),
(211, 'Chicken Breast', 165, 31, 0, 3.6, 0),
(212, 'Carrot', 41, 0.9, 10, 0.2, 2.8),
(213, 'Peas', 81, 5.4, 14.5, 0.4, 5.7),
(214, 'Egg', 143, 13, 1.1, 10, 0),
(215, 'Soy Sauce', 53, 8.1, 4.9, 0.6, 0.8),
(216, 'Vegetable Oil', 884, 0, 0, 100, 0),
(217, 'All-purpose Flour', 364, 10, 76, 1, 2.7),
(218, 'Water', 0, 0, 0, 0, 0),
(219, 'Condensed Milk', 321, 7.9, 54.4, 8.7, 0),
(220, 'Salt', 0, 0, 0, 0, 0),
(221, 'Sugar', 387, 0, 100, 0, 0),
(222, 'Vegetable Oil', 884, 0, 0, 100, 0),
(223, 'Ghee', 876, 0.3, 0, 99.5, 0);

-- --------------------------------------------------------

--
-- Table structure for table `meal_plans`
--

CREATE TABLE `meal_plans` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `meal_date` date NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `total_calories` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `meal_plans`
--

INSERT INTO `meal_plans` (`id`, `user_id`, `meal_date`, `created_at`, `total_calories`) VALUES
(1, 1, '2025-04-24', '2025-04-24 13:53:37', 255),
(2, 2, '2025-04-24', '2025-04-24 14:23:15', 510),
(5, 2, '2025-04-25', '2025-04-24 15:23:36', 2689),
(6, 2, '2025-04-26', '2025-04-25 04:44:51', 85),
(7, 2, '2025-04-27', '2025-04-25 05:37:20', 217),
(8, 2, '2025-04-28', '2025-04-28 14:57:32', 1172),
(9, 2, '2025-04-29', '2025-04-28 15:08:50', 85),
(10, 2, '2025-04-30', '2025-04-30 03:31:07', 780),
(11, 2, '2025-05-01', '2025-04-30 03:40:10', 477),
(12, 2, '2025-05-10', '2025-05-10 04:46:16', 85),
(13, 2, '2025-05-11', '2025-05-11 09:44:25', 632),
(14, 24, '2025-05-12', '2025-05-12 16:37:01', 147),
(15, 24, '2025-05-13', '2025-05-12 16:37:26', 477),
(16, 2, '2025-05-13', '2025-05-13 01:41:52', 695),
(17, 2, '2025-06-25', '2025-06-25 06:44:10', 600),
(18, 2, '2025-07-03', '2025-07-03 06:51:24', 1204),
(19, 2, '2025-07-02', '2025-07-03 13:27:26', 602),
(20, 2, '2025-07-04', '2025-07-03 13:45:02', 602),
(21, 2, '2025-07-11', '2025-07-07 05:42:18', 2706),
(22, 2, '2025-07-12', '2025-07-07 07:03:18', 602),
(23, 2, '2025-07-10', '2025-07-08 06:25:09', 2182),
(24, 2, '2025-07-07', '2025-07-09 07:02:16', 602),
(25, 2, '2025-07-13', '2025-07-09 07:02:41', 3234),
(27, 2, '2025-07-19', '2025-07-14 04:20:07', 1617),
(29, 2, '2025-07-16', '2025-07-14 05:28:38', 937),
(30, 2, '2025-07-15', '2025-07-20 06:20:38', 6750),
(31, 2, '2025-07-30', '2025-07-20 13:57:45', 3038),
(33, 2, '2025-07-20', '2025-07-21 05:30:24', 974),
(34, 2, '2025-07-21', '2025-07-21 05:53:59', 2551),
(40, 2, '2025-07-27', '2025-07-26 10:34:05', 3193),
(41, 2, '2025-07-25', '2025-07-26 14:26:37', 0),
(53, 2, '2025-07-28', '2025-07-27 07:22:36', 3234),
(54, 2, '2025-07-29', '2025-07-27 07:40:20', 487),
(55, 2, '2025-07-31', '2025-07-31 03:55:31', 1382),
(64, 2, '2025-08-02', '2025-08-02 05:19:07', 487),
(69, 2, '2025-08-03', '2025-08-03 05:23:29', 895);

-- --------------------------------------------------------

--
-- Table structure for table `meal_plan_nutrition`
--

CREATE TABLE `meal_plan_nutrition` (
  `id` int(11) NOT NULL,
  `meal_plan_recipe_id` int(11) NOT NULL,
  `member_id` int(11) NOT NULL,
  `portion_multiplier` decimal(5,2) NOT NULL,
  `calories` decimal(10,2) DEFAULT NULL,
  `protein` decimal(10,2) DEFAULT NULL,
  `fat` decimal(10,2) DEFAULT NULL,
  `carbs` decimal(10,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `meal_plan_nutrition`
--

INSERT INTO `meal_plan_nutrition` (`id`, `meal_plan_recipe_id`, `member_id`, `portion_multiplier`, `calories`, `protein`, `fat`, `carbs`) VALUES
(1, 27, 1, 1.20, 540.00, 30.60, 14.76, 66.24),
(3, 28, 1, 1.00, 450.00, 25.50, 12.30, 55.20),
(5, 29, 1, 1.00, 250.00, 14.17, 6.83, 30.67),
(7, 30, 1, 1.00, 250.00, 14.17, 6.83, 30.67),
(11, 33, 1, 1.00, 250.00, 14.17, 6.83, 30.67),
(13, 34, 1, 1.00, 250.00, 14.17, 6.83, 30.67),
(15, 35, 1, 1.00, 214.29, 12.14, 5.86, 26.29),
(29, 48, 1, 1.07, 252.07, 21.23, 2.43, 34.58),
(32, 54, 3, 0.79, 215.66, 18.16, 2.08, 29.59),
(33, 55, 1, 1.07, 311.56, 13.08, 10.36, 38.97),
(34, 56, 3, 0.79, 215.66, 18.16, 2.08, 29.59),
(36, 63, 3, 0.79, 215.66, 18.16, 2.08, 29.59),
(37, 72, 3, 0.79, 266.55, 11.19, 8.87, 33.34),
(38, 73, 1, 1.07, 252.07, 21.23, 2.43, 34.58),
(39, 79, 1, 1.20, 328.42, 13.79, 10.93, 41.08),
(41, 88, 3, 1.07, 211.10, 5.18, 3.09, 39.33),
(42, 92, 1, 1.07, 139.12, 3.41, 2.04, 25.92),
(43, 92, 3, 1.07, 139.12, 3.41, 2.04, 25.92);

-- --------------------------------------------------------

--
-- Table structure for table `meal_plan_recipe`
--

CREATE TABLE `meal_plan_recipe` (
  `id` int(11) NOT NULL,
  `meal_plan_id` int(11) NOT NULL,
  `recipe_id` int(11) NOT NULL,
  `category` varchar(50) NOT NULL,
  `is_user_recipe` tinyint(1) DEFAULT 1,
  `user_id` int(11) NOT NULL,
  `date` date DEFAULT NULL,
  `time` time NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `meal_plan_recipe`
--

INSERT INTO `meal_plan_recipe` (`id`, `meal_plan_id`, `recipe_id`, `category`, `is_user_recipe`, `user_id`, `date`, `time`) VALUES
(2, 18, 1, 'Breakfast', 1, 0, NULL, '00:00:00'),
(3, 18, 1, 'Lunch', 1, 0, NULL, '00:00:00'),
(4, 19, 1, 'Breakfast', 1, 0, NULL, '00:00:00'),
(6, 21, 1, 'Lunch', 1, 0, '2025-07-11', '00:00:00'),
(7, 21, 13, 'Lunch', 1, 0, '2025-07-11', '00:00:00'),
(8, 22, 1, 'Breakfast', 1, 0, '2025-07-12', '00:00:00'),
(9, 21, 1, 'Breakfast', 1, 0, '2025-07-11', '00:00:00'),
(10, 21, 13, 'Dinner', 1, 0, '2025-07-11', '00:00:00'),
(11, 23, 13, 'Lunch', 1, 0, '2025-07-10', '00:00:00'),
(12, 23, 1, 'Breakfast', 1, 2, '2025-07-10', '00:00:00'),
(13, 24, 1, 'Breakfast', 1, 2, '2025-07-07', '00:00:00'),
(14, 25, 1, 'Breakfast', 1, 2, '2025-07-13', '00:00:00'),
(15, 25, 1, 'Lunch', 1, 2, '2025-07-13', '00:00:00'),
(16, 23, 20, 'Dinner', 1, 2, '2025-07-10', '00:00:00'),
(17, 25, 13, 'Dinner', 1, 2, '2025-07-13', '00:00:00'),
(19, 25, 13, 'Dinner', 1, 2, '2025-07-13', '00:00:00'),
(20, 21, 1, 'Lunch', 1, 2, '2025-07-11', '00:00:00'),
(21, 27, 19, 'Breakfast', 1, 2, '2025-07-19', '00:00:00'),
(22, 27, 20, 'Breakfast', 1, 2, '2025-07-19', '00:00:00'),
(23, 29, 19, 'Breakfast', 1, 2, '2025-07-16', '00:00:00'),
(24, 30, 13, 'Lunch', 1, 2, '2025-07-15', '00:00:00'),
(25, 30, 13, 'Lunch', 1, 2, '2025-07-15', '00:00:00'),
(26, 30, 13, 'Lunch', 1, 2, '2025-07-15', '00:00:00'),
(27, 30, 13, 'Lunch', 1, 2, '2025-07-15', '00:00:00'),
(28, 30, 13, 'Lunch', 1, 2, '2025-07-15', '00:00:00'),
(29, 30, 13, 'Lunch', 1, 2, '2025-07-15', '00:00:00'),
(30, 30, 13, 'Lunch', 1, 2, '2025-07-15', '00:00:00'),
(31, 30, 13, 'Lunch', 1, 2, '2025-07-15', '00:00:00'),
(32, 30, 13, 'Lunch', 1, 2, '2025-07-15', '00:00:00'),
(33, 30, 13, 'Lunch', 1, 2, '2025-07-15', '00:00:00'),
(34, 30, 13, 'Lunch', 1, 2, '2025-07-15', '00:00:00'),
(35, 30, 13, 'Lunch', 1, 2, '2025-07-15', '00:00:00'),
(36, 30, 13, 'Lunch', 1, 2, '2025-07-15', '00:00:00'),
(37, 30, 13, 'Lunch', 1, 2, '2025-07-15', '00:00:00'),
(38, 30, 13, 'Lunch', 1, 2, '2025-07-15', '00:00:00'),
(39, 29, 13, 'Lunch', 1, 2, '2025-07-16', '00:00:00'),
(48, 33, 19, 'Breakfast', 1, 2, '2025-07-20', '00:00:00'),
(54, 34, 19, 'Lunch', 1, 2, '2025-07-21', '00:00:00'),
(55, 34, 1, 'Dinner', 1, 2, '2025-07-21', '00:00:00'),
(56, 34, 19, 'Breakfast', 1, 2, '2025-07-21', '00:00:00'),
(60, 33, 19, 'Breakfast', 1, 2, '2025-07-20', '00:00:00'),
(63, 53, 19, 'Breakfast', 1, 2, '2025-07-28', '00:00:00'),
(64, 53, 20, 'Lunch', 1, 2, '2025-07-28', '00:00:00'),
(67, 34, 19, 'Breakfast', 1, 2, '2025-07-21', '00:00:00'),
(68, 53, 20, 'Breakfast', 1, 2, '2025-07-28', '00:00:00'),
(69, 54, 19, 'Breakfast', 1, 2, '2025-07-29', '00:00:00'),
(72, 40, 1, 'Breakfast', 1, 2, '2025-07-27', '00:00:00'),
(73, 40, 19, 'Dinner', 1, 2, '2025-07-27', '00:00:00'),
(75, 53, 20, 'Dinner', 1, 2, '2025-07-28', '00:00:00'),
(76, 54, 20, 'Lunch', 1, 2, '2025-07-29', '00:00:00'),
(77, 31, 19, 'Breakfast', 1, 2, '2025-07-30', '00:00:00'),
(79, 31, 1, 'Lunch', 1, 2, '2025-07-30', '12:30:00'),
(81, 31, 19, 'Breakfast', 1, 2, '2025-07-30', '00:00:00'),
(82, 31, 19, 'Dinner', 1, 2, '2025-07-30', '00:00:00'),
(83, 31, 19, 'Dinner', 1, 2, '2025-07-30', '00:00:00'),
(84, 31, 19, 'Breakfast', 1, 2, '2025-07-30', '00:00:00'),
(87, 55, 19, 'Breakfast', 1, 2, '2025-07-31', '08:00:00'),
(88, 55, 20, 'Breakfast', 1, 2, '2025-07-31', '12:00:00'),
(89, 55, 20, 'Breakfast', 1, 2, '2025-07-31', '08:00:00'),
(90, 64, 19, 'Breakfast', 1, 2, '2025-08-02', '07:00:00'),
(92, 69, 20, 'Breakfast', 1, 2, '2025-08-03', '07:00:00'),
(93, 69, 19, 'Breakfast', 1, 2, '2025-08-03', '08:00:00');

-- --------------------------------------------------------

--
-- Table structure for table `meal_plan_recipe_member`
--

CREATE TABLE `meal_plan_recipe_member` (
  `id` int(11) NOT NULL,
  `meal_plan_recipe_id` int(11) NOT NULL,
  `member_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `meal_plan_recipe_member`
--

INSERT INTO `meal_plan_recipe_member` (`id`, `meal_plan_recipe_id`, `member_id`) VALUES
(1, 20, 1),
(3, 21, 1),
(6, 24, 1),
(8, 25, 1),
(10, 26, 1),
(12, 27, 1),
(14, 28, 1),
(16, 29, 1),
(18, 30, 1),
(20, 31, 1),
(22, 32, 1),
(24, 33, 1),
(26, 34, 1),
(28, 35, 1),
(30, 36, 1),
(32, 37, 1),
(34, 38, 1),
(36, 39, 1),
(54, 48, 1),
(60, 54, 3),
(61, 55, 1),
(62, 56, 3),
(67, 63, 3),
(68, 72, 3),
(69, 73, 1),
(70, 79, 1),
(72, 88, 3),
(73, 92, 1),
(74, 92, 3);

-- --------------------------------------------------------

--
-- Table structure for table `meal_plan_recipe_user`
--

CREATE TABLE `meal_plan_recipe_user` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `meal_plan_id` int(11) NOT NULL,
  `meal_plan_recipe_id` int(11) NOT NULL,
  `recipe_id` int(11) NOT NULL,
  `meal_date` date NOT NULL,
  `category` varchar(50) NOT NULL,
  `portion_multiplier` decimal(5,2) NOT NULL,
  `percent` decimal(5,2) NOT NULL,
  `calories` decimal(10,2) DEFAULT NULL,
  `protein` decimal(10,2) DEFAULT NULL,
  `fat` decimal(10,2) DEFAULT NULL,
  `carbs` decimal(10,2) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `meal_plan_recipe_user`
--

INSERT INTO `meal_plan_recipe_user` (`id`, `user_id`, `meal_plan_id`, `meal_plan_recipe_id`, `recipe_id`, `meal_date`, `category`, `portion_multiplier`, `percent`, `calories`, `protein`, `fat`, `carbs`, `created_at`) VALUES
(1, 2, 30, 30, 13, '2025-07-15', 'Lunch', 1.00, 55.56, 250.00, 14.17, 6.83, 30.67, '2025-07-20 08:33:30'),
(2, 2, 30, 31, 13, '2025-07-15', 'Lunch', 1.00, 55.56, 250.00, 14.17, 6.83, 30.67, '2025-07-20 08:40:18'),
(3, 2, 30, 32, 13, '2025-07-15', 'Lunch', 1.00, 55.56, 250.00, 14.17, 6.83, 30.67, '2025-07-20 08:40:44'),
(4, 2, 30, 32, 13, '2025-07-15', 'Lunch', 1.00, 55.56, 250.00, 14.17, 6.83, 30.67, '2025-07-20 08:40:44'),
(5, 2, 30, 34, 13, '2025-07-15', 'Lunch', 1.00, 55.56, 250.00, 14.17, 6.83, 30.67, '2025-07-20 08:53:13'),
(6, 2, 30, 35, 13, '2025-07-15', 'Lunch', 1.00, 47.62, 214.29, 12.14, 5.86, 26.29, '2025-07-20 08:54:57'),
(7, 2, 30, 36, 13, '2025-07-15', 'Lunch', 1.00, 47.62, 214.29, 12.14, 5.86, 26.29, '2025-07-20 09:03:37'),
(8, 2, 30, 36, 13, '2025-07-15', 'Lunch', 1.00, 47.62, 214.29, 12.14, 5.86, 26.29, '2025-07-20 09:03:37'),
(9, 2, 30, 37, 13, '2025-07-15', 'Lunch', 1.00, 47.62, 214.29, 12.14, 5.86, 26.29, '2025-07-20 13:54:30'),
(10, 2, 30, 38, 13, '2025-07-15', 'Lunch', 1.00, 47.62, 214.29, 12.14, 5.86, 26.29, '2025-07-20 13:54:53'),
(11, 2, 30, 38, 13, '2025-07-15', 'Lunch', 1.00, 47.62, 214.29, 12.14, 5.86, 26.29, '2025-07-20 13:54:53'),
(12, 2, 29, 39, 13, '2025-07-16', 'Lunch', 1.00, 47.62, 214.29, 12.14, 5.86, 26.29, '2025-07-20 13:57:08'),
(13, 2, 29, 39, 13, '2025-07-16', 'Lunch', 1.00, 47.62, 214.29, 12.14, 5.86, 26.29, '2025-07-20 13:57:08'),
(23, 2, 33, 48, 19, '2025-07-20', 'Breakfast', 1.00, 48.25, 235.06, 19.80, 2.27, 32.25, '2025-07-21 05:30:24'),
(28, 2, 34, 54, 19, '2025-07-21', 'Lunch', 1.00, 55.73, 271.47, 22.87, 2.62, 37.24, '2025-07-26 10:34:56'),
(29, 2, 34, 55, 1, '2025-07-21', 'Dinner', 1.00, 48.25, 290.54, 12.20, 9.67, 36.34, '2025-07-26 10:35:24'),
(30, 2, 34, 56, 19, '2025-07-21', 'Breakfast', 1.00, 55.73, 271.47, 22.87, 2.62, 37.24, '2025-07-26 10:38:49'),
(31, 2, 33, 60, 19, '2025-07-20', 'Breakfast', 1.00, 100.00, 487.13, 41.03, 4.70, 66.83, '2025-07-26 14:46:34'),
(34, 2, 53, 63, 19, '2025-07-28', 'Breakfast', 1.00, 55.73, 271.47, 22.87, 2.62, 37.24, '2025-07-27 07:23:30'),
(35, 2, 53, 64, 20, '2025-07-28', 'Lunch', 1.00, 100.00, 1129.80, 25.05, 27.42, 190.00, '2025-07-27 07:24:42'),
(38, 2, 34, 67, 19, '2025-07-21', 'Breakfast', 1.00, 100.00, 487.13, 41.03, 4.70, 66.83, '2025-07-27 07:32:35'),
(39, 2, 53, 68, 20, '2025-07-28', 'Breakfast', 1.00, 100.00, 1129.80, 25.05, 27.42, 190.00, '2025-07-27 07:32:59'),
(40, 2, 54, 69, 19, '2025-07-29', 'Breakfast', 1.00, 100.00, 487.13, 41.03, 4.70, 66.83, '2025-07-27 07:40:20'),
(43, 2, 40, 72, 1, '2025-07-27', 'Breakfast', 1.00, 55.73, 335.55, 14.09, 11.16, 41.97, '2025-07-27 07:45:34'),
(44, 2, 40, 73, 19, '2025-07-27', 'Dinner', 1.00, 48.25, 235.06, 19.80, 2.27, 32.25, '2025-07-27 07:46:04'),
(48, 2, 31, 77, 19, '2025-07-30', 'Breakfast', 1.00, 100.00, 487.13, 41.03, 4.70, 66.83, '2025-07-30 15:23:57'),
(50, 2, 31, 79, 1, '2025-07-30', 'Lunch', 1.00, 45.45, 273.68, 11.49, 9.10, 34.23, '2025-07-30 15:56:03'),
(52, 2, 31, 81, 19, '2025-07-30', 'Breakfast', 1.00, 100.00, 487.13, 41.03, 4.70, 66.83, '2025-07-30 15:57:49'),
(53, 2, 31, 82, 19, '2025-07-30', 'Dinner', 1.00, 100.00, 487.13, 41.03, 4.70, 66.83, '2025-07-30 16:07:57'),
(54, 2, 31, 83, 19, '2025-07-30', 'Dinner', 1.00, 100.00, 487.13, 41.03, 4.70, 66.83, '2025-07-30 16:24:31'),
(55, 2, 31, 84, 19, '2025-07-30', 'Breakfast', 1.00, 100.00, 487.13, 41.03, 4.70, 66.83, '2025-07-30 16:34:10'),
(58, 2, 55, 87, 19, '2025-07-31', 'Breakfast', 1.00, 100.00, 487.13, 41.03, 4.70, 66.83, '2025-07-31 04:08:04'),
(59, 2, 55, 88, 20, '2025-07-31', 'Breakfast', 1.00, 48.25, 196.86, 4.83, 2.89, 36.67, '2025-07-31 04:22:19'),
(61, 2, 64, 90, 19, '2025-08-02', 'Breakfast', 1.00, 100.00, 487.13, 41.03, 4.70, 66.83, '2025-08-02 05:19:07'),
(63, 2, 69, 92, 20, '2025-08-03', 'Breakfast', 1.00, 31.80, 129.73, 3.18, 1.90, 24.17, '2025-08-03 05:23:43'),
(64, 2, 69, 93, 19, '2025-08-03', 'Breakfast', 1.00, 100.00, 487.13, 41.03, 4.70, 66.83, '2025-08-03 06:00:15');

-- --------------------------------------------------------

--
-- Table structure for table `recipes`
--

CREATE TABLE `recipes` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `time_recipe` varchar(50) DEFAULT NULL,
  `servings` int(11) DEFAULT 1,
  `image_url` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `cuisine_type` varchar(50) NOT NULL,
  `dietary` varchar(50) NOT NULL,
  `visibility` enum('public','community','private') NOT NULL DEFAULT 'public',
  `is_user_recipe` tinyint(1) NOT NULL DEFAULT 1,
  `customize` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `recipes`
--

INSERT INTO `recipes` (`id`, `user_id`, `title`, `description`, `time_recipe`, `servings`, `image_url`, `created_at`, `cuisine_type`, `dietary`, `visibility`, `is_user_recipe`, `customize`) VALUES
(1, 1, 'Spaghetti Carbonara', 'Classic Italian pasta with creamy sauce.', '25 mins', 2, 'https://spoonacular.com/recipeImages/716429-556x370.jpg', '2025-06-25 06:49:11', 'Italian', 'Non Vegan', 'public', 1, 0),
(13, 1, 'Chicken Fried Rice', 'Classic Asian-style fried rice with chicken and vegetables', '25 mins', 4, 'https://spoonacular.com/recipeImages/716426-556x370.jpg', '2025-07-07 06:09:41', 'Chinese', 'Non Vegan', 'public', 1, 0),
(19, 2, 'Chicken Fried Rice', 'A quick and easy fried rice with chicken, vegetables, and soy sauce.', '25 minutes', 1, 'https://spoonacular.com/recipeImages/123456-556x370.jpg', '2025-07-10 04:12:20', 'Chinese', 'Non-Vegan', 'public', 1, 0),
(20, 2, 'Roti Canai', 'A popular Malaysian Flatbread that\\\'s crispy on the out side and soft inside', '1 Hours', 1, 'http://192.168.0.130/Final%20Year%20Project/uploads/recipe_1752121274_2.jpg', '2025-07-10 04:21:14', 'Malay', 'Non Vegan', 'public', 1, 0);

-- --------------------------------------------------------

--
-- Table structure for table `recipe_ingredients`
--

CREATE TABLE `recipe_ingredients` (
  `id` int(11) NOT NULL,
  `recipe_id` int(11) NOT NULL,
  `ingredient_name` varchar(100) NOT NULL,
  `amount_value` decimal(10,2) DEFAULT NULL,
  `unit` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `recipe_ingredients`
--

INSERT INTO `recipe_ingredients` (`id`, `recipe_id`, `ingredient_name`, `amount_value`, `unit`) VALUES
(8, 1, 'Spaghetti', 120.00, 'g'),
(9, 1, 'Egg yolk', 2.00, 'pcs'),
(10, 1, 'Pecorino Romano', 25.00, 'g'),
(11, 1, 'Guanciale', 40.00, 'g'),
(12, 1, 'Black pepper', NULL, NULL),
(13, 1, 'Salt', NULL, NULL),
(14, 1, 'Choy Sum', NULL, NULL),
(15, 1, 'Garlic', NULL, NULL),
(16, 1, 'Chinese Broccoli', NULL, NULL),
(17, 13, 'Cooked rice', NULL, NULL),
(18, 13, 'Chicken breast', NULL, NULL),
(19, 13, 'Eggs', NULL, NULL),
(20, 13, 'Spring Onion', NULL, NULL),
(21, 13, 'Soy sauce', NULL, NULL),
(22, 13, 'Vegetable oil', NULL, NULL),
(23, 13, 'Garlic', NULL, NULL),
(24, 13, 'Carrots', NULL, NULL),
(25, 1, 'Choy Sum', NULL, NULL),
(40, 19, 'Cooked Rice', 200.00, NULL),
(41, 19, 'Chicken Breast', 100.00, NULL),
(42, 19, 'Carrot', 50.00, NULL),
(43, 19, 'Peas', 30.00, NULL),
(44, 19, 'Egg', 1.00, NULL),
(45, 19, 'Soy Sauce', 2.00, NULL),
(46, 19, 'Oil', 1.00, NULL),
(47, 20, 'All-purpose Flour', 100.00, 'g'),
(48, 20, 'Water', 130.00, 'g'),
(49, 20, 'Vegetable Oil', 2.00, 'g'),
(50, 20, 'Ghee', 3.00, 'g');

-- --------------------------------------------------------

--
-- Table structure for table `recipe_instructions`
--

CREATE TABLE `recipe_instructions` (
  `id` int(11) NOT NULL,
  `recipe_id` int(11) NOT NULL,
  `step_number` int(11) NOT NULL,
  `instruction_text` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `recipe_instructions`
--

INSERT INTO `recipe_instructions` (`id`, `recipe_id`, `step_number`, `instruction_text`) VALUES
(7, 1, 1, 'Boil spaghetti in salted water until al dente.'),
(8, 1, 2, 'Cook pancetta in a pan until crispy.'),
(9, 1, 3, 'Beat eggs with grated Parmesan and set aside.'),
(10, 1, 4, 'Drain spaghetti and mix quickly with pancetta and garlic.'),
(11, 1, 5, 'Remove pan from heat and stir in egg-cheese mixture.'),
(12, 1, 6, 'Season with black pepper and serve immediately.'),
(13, 13, 1, 'Heat oil in a wok or large frying pan over medium-high heat.'),
(14, 13, 2, 'Add diced chicken and cook until no longer pink, then remove and set aside.'),
(15, 13, 3, 'In the same wok, scramble the eggs, then remove and set aside with the chicken.'),
(16, 13, 4, 'Add a bit more oil if needed, then sauté garlic and carrots for 2 minutes.'),
(17, 13, 5, 'Add rice and stir-fry for 3-4 minutes, breaking up any clumps.'),
(18, 13, 6, 'Return chicken and eggs to the wok, add soy sauce and spring onions, mix well.'),
(19, 13, 7, 'Cook for another 2 minutes, then serve hot.'),
(30, 19, 1, 'Heat oil in a pan and scramble the egg. Set aside.'),
(31, 19, 2, 'Stir-fry diced chicken until cooked.'),
(32, 19, 3, 'Add carrots and peas, cook for 3–4 minutes.'),
(33, 19, 4, 'Add cooked rice, scrambled egg, and soy sauce. Mix well.'),
(34, 19, 5, 'Cook for another 2–3 minutes and serve hot.'),
(35, 20, 1, 'cook it');

-- --------------------------------------------------------

--
-- Table structure for table `recipe_nutrition`
--

CREATE TABLE `recipe_nutrition` (
  `id` int(11) NOT NULL,
  `recipe_id` int(11) NOT NULL,
  `calories` decimal(10,2) DEFAULT 0.00,
  `protein` decimal(10,2) DEFAULT 0.00,
  `fat` decimal(10,2) DEFAULT 0.00,
  `carbohydrates` decimal(10,2) DEFAULT 0.00,
  `fiber` decimal(10,2) DEFAULT 0.00,
  `sugar` decimal(10,2) DEFAULT 0.00,
  `sodium` decimal(10,2) DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `recipe_nutrition`
--

INSERT INTO `recipe_nutrition` (`id`, `recipe_id`, `calories`, `protein`, `fat`, `carbohydrates`, `fiber`, `sugar`, `sodium`) VALUES
(2, 1, 602.10, 25.28, 20.03, 75.31, 3.18, 4.00, 900.00),
(3, 13, 450.00, 25.50, 12.30, 55.20, 3.50, 2.10, 850.00),
(4, 19, 487.13, 41.03, 4.70, 66.83, 0.00, 0.00, 0.00),
(5, 20, 407.96, 10.01, 5.98, 76.00, 2.70, 0.00, 0.00);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(30) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(100) NOT NULL,
  `profile_picture` varchar(255) DEFAULT NULL,
  `family_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `password`, `email`, `profile_picture`, `family_id`) VALUES
(1, 'melvin7787', 'melvin2386', 'melvin7787@gmail.com', NULL, NULL),
(2, 'melvin1233', '$2y$10$7pZm5PI68Q97N0oGwsxuiewplPLViHExcd6.pZNOY0dN2lyJivSri', 'melvin123@gmail.com', 'http://192.168.0.130/Final%20Year%20Project/uploads/profile_681ed6d7db3e1.1746851543.jpg', NULL),
(3, 'melvin111', '$2y$10$JwwHKnqGAAgzA2in5NpvpO2FV0R1k1.o4.MQ.zGKMaraOwAmm3ice', '1234@gmail.com', NULL, NULL),
(4, '123', '$2y$10$qXvDpspzo98O3sALPwcl2eLDsB6vQdshJlk./XpYQScgr6ovkBCYa', '9999@gmail.com', NULL, NULL),
(5, '22222', '$2y$10$1piIrA84MT4yD5S65RSaLulort4cZPE80DNBXwnPgHskr5PsiXr4C', '22222@gmail.com', NULL, NULL),
(6, '111', '$2y$10$wQPEc2AxO/lCQj3L9jOKUuoCqG/D/VTNegB7UU.ErtB27SjFNZZuq', '1111@gmail.com', 'uploads/profile_6_1743154574png', NULL),
(7, '222', '$2y$10$rK.a1hb0fOIuHn0AXHPkmetcbpYIwbNYahSxHn/R1Kt7oZ6529CQ2', '222@gmail.com', NULL, NULL),
(8, '333', '$2y$10$W8mIFZshxP3aBe1S/RqbM.YWOAC67xAJXGUjp2LRveUPaQ257ny0e', '333@gmail.com', NULL, NULL),
(9, '555', '$2y$10$u4q8ebSYjp/tonj68AfBaOLY7lYhkuRFAYI85opLR8X4Pa4VeiB5W', '555@gmail.com', NULL, NULL),
(10, '666', '$2y$10$fRVVKPai7eY591xHweLMDOptq5bGEMAYn79xL3.Kp.vuKckJb8lNe', '666@gmail.com', NULL, NULL),
(11, '777', '$2y$10$45VeBH4Y16eh6ylo.b5d3OROi7pXgIJmuuyPhbwb8qPreKypJuVAW', '777@gmail.com', NULL, NULL),
(12, '888', '$2y$10$.sYnPTKBVXW.d4ImRe/4Eehm.AILYc0niQZXKlHsoGcUyxvtwTEGa', '888@gmail.com', NULL, NULL),
(19, '999', '$2y$10$kvVO9Gr7t9nqZjEzlvamHOLgj.SedPfAXSZ8ztaVdjzIDwvV0Hswi', '999@gmail.com', NULL, NULL),
(20, 'yvette', '$2y$10$RTziXZ0NDLyRpPENd1biVen..AaOLCCXpYR3XHJ7BW5sjnwjkYVke', 'yvette@gmail.com', NULL, NULL),
(21, 'melvin7218', '$2y$10$Sd0rDSZpF.lnVgfAfYGdLe9kV6B9G35Melev..4OGVkj7BkyvascG', 'abc123gmail.com', NULL, NULL),
(22, 'Dawen123', '$2y$10$Jt9W4PaCqKupFTha14qVwuZg4VY7D7M4CZG9DfH01TcA6R3MEZzWq', 'zzz123@gmail.com', NULL, NULL),
(24, 'Zhing Yee', '$2y$10$ULLhvwwY.dUpftpDL44tkegYfWSVgOoQr7zS5DYZBpT6s84XU7CyC', 'zhingYee@gmail.com', 'http://192.168.0.7/Final%20Year%20Project/uploads/profile_682223e2168db.1747067874.jpg', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `user_exercise`
--

CREATE TABLE `user_exercise` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `exercise_id` int(11) NOT NULL,
  `duration_minutes` int(11) NOT NULL,
  `calories_burned` decimal(10,2) NOT NULL,
  `exercise_date` date NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `starting_time` time NOT NULL,
  `ending_time` time NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user_exercise`
--

INSERT INTO `user_exercise` (`id`, `user_id`, `exercise_id`, `duration_minutes`, `calories_burned`, `exercise_date`, `created_at`, `starting_time`, `ending_time`) VALUES
(1, 2, 24, 60, 270.00, '2025-07-30', '2025-07-30 15:18:07', '00:00:00', '00:00:00'),
(2, 2, 30, 60, 270.00, '2025-07-31', '2025-07-30 15:21:02', '00:00:00', '00:00:00'),
(3, 2, 30, 60, 270.00, '2025-07-31', '2025-07-31 05:37:10', '00:00:00', '00:00:00'),
(4, 2, 30, 60, 270.00, '2025-08-02', '2025-08-02 05:31:31', '00:00:00', '00:00:00'),
(5, 2, 16, 45, 135.00, '2025-08-02', '2025-08-02 06:08:43', '00:00:00', '00:00:00'),
(6, 2, 30, 60, 270.00, '2025-08-03', '2025-08-03 05:24:05', '00:00:00', '00:00:00'),
(7, 2, 30, 60, 270.00, '2025-08-03', '2025-08-03 06:01:32', '00:00:00', '00:00:00'),
(8, 2, 30, 60, 270.00, '2025-08-03', '2025-08-03 06:21:06', '00:00:00', '00:00:00');

-- --------------------------------------------------------

--
-- Table structure for table `user_exercise_member`
--

CREATE TABLE `user_exercise_member` (
  `id` int(11) NOT NULL,
  `user_exercise_id` int(11) NOT NULL,
  `member_id` int(11) NOT NULL,
  `duration_minutes` int(11) NOT NULL,
  `calories_burned` decimal(10,2) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `starting_time` time NOT NULL,
  `ending_time` time NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user_exercise_member`
--

INSERT INTO `user_exercise_member` (`id`, `user_exercise_id`, `member_id`, `duration_minutes`, `calories_burned`, `created_at`, `starting_time`, `ending_time`) VALUES
(1, 5, 1, 45, 135.00, '2025-08-02 06:08:43', '00:00:00', '00:00:00'),
(2, 6, 1, 60, 270.00, '2025-08-03 05:24:05', '00:00:00', '00:00:00'),
(3, 7, 1, 60, 270.00, '2025-08-03 06:01:32', '00:00:00', '00:00:00');

-- --------------------------------------------------------

--
-- Table structure for table `user_members`
--

CREATE TABLE `user_members` (
  `member_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `member_name` varchar(255) NOT NULL,
  `age` int(11) DEFAULT NULL,
  `height` decimal(10,2) DEFAULT NULL,
  `weight` decimal(10,2) DEFAULT NULL,
  `gender` varchar(10) DEFAULT NULL,
  `activity_factor` decimal(10,2) DEFAULT 1.20,
  `bmr` decimal(10,2) DEFAULT NULL,
  `tdee` decimal(10,2) DEFAULT NULL,
  `cuisine_type` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user_members`
--

INSERT INTO `user_members` (`member_id`, `user_id`, `member_name`, `age`, `height`, `weight`, `gender`, `activity_factor`, `bmr`, `tdee`, `cuisine_type`) VALUES
(1, 2, 'Alice', 30, 165.00, 60.00, 'female', 1.55, 1383.68, 2144.71, 'chinese'),
(3, 2, 'Zhing Yee', 21, 163.00, 50.00, 'female', 1.20, 1323.99, 1588.78, 'korean');

-- --------------------------------------------------------

--
-- Table structure for table `user_preferences`
--

CREATE TABLE `user_preferences` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `allergy_ingredients` text DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `height` decimal(10,2) DEFAULT NULL COMMENT 'in cm',
  `weight` decimal(10,2) DEFAULT NULL COMMENT 'in kg',
  `gender` varchar(10) DEFAULT NULL,
  `activity_factor` decimal(10,2) DEFAULT 1.20,
  `bmr` decimal(10,2) DEFAULT NULL COMMENT 'Basal Metabolic Rate',
  `tdee` decimal(10,2) DEFAULT NULL COMMENT 'Total Daily Energy Expenditure',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `cuisine_type` varchar(255) DEFAULT NULL,
  `protein` double(11,2) NOT NULL,
  `fat` double(11,2) NOT NULL,
  `carbohydrates` double(11,2) NOT NULL,
  `fiber` double(11,2) NOT NULL,
  `sugar` double(11,2) NOT NULL,
  `sodium` double(11,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user_preferences`
--

INSERT INTO `user_preferences` (`id`, `user_id`, `allergy_ingredients`, `age`, `height`, `weight`, `gender`, `activity_factor`, `bmr`, `tdee`, `created_at`, `updated_at`, `cuisine_type`, `protein`, `fat`, `carbohydrates`, `fiber`, `sugar`, `sodium`) VALUES
(1, 2, 'peanuts', 19, 174.00, 60.00, 'Male', 1.73, 1597.50, 2755.69, '2025-04-13 06:45:49', '2025-05-13 02:51:18', 'thai', 0.00, 0.00, 0.00, 0.00, 0.00, 0.00),
(3, 19, 'peanuts', 22, 174.00, 56.00, 'Male', 1.55, 1542.50, 2390.88, '2025-04-13 11:15:20', '2025-04-13 11:15:20', NULL, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00),
(4, 20, 'no', 21, 162.00, 51.00, 'Female', 1.55, 1256.50, 1947.58, '2025-04-13 14:02:53', '2025-04-13 14:02:53', NULL, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00),
(5, 22, 'shellfish', 15, 165.00, 50.00, 'Male', 1.20, 1461.25, 1753.50, '2025-04-30 03:37:04', '2025-04-30 03:37:04', NULL, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00),
(10, 24, 'Fish, Shellfish', 21, 162.00, 52.00, 'Male', 1.20, 1432.50, 1719.00, '2025-05-12 15:58:43', '2025-05-12 16:38:25', 'chinese', 0.00, 0.00, 0.00, 0.00, 0.00, 0.00);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `favorite_recipes`
--
ALTER TABLE `favorite_recipes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_favorite` (`user_id`,`recipe_id`),
  ADD KEY `recipe_id` (`recipe_id`);

--
-- Indexes for table `grocery_lists`
--
ALTER TABLE `grocery_lists`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `recipe_id` (`recipe_id`);

--
-- Indexes for table `ingredient_conversions`
--
ALTER TABLE `ingredient_conversions`
  ADD PRIMARY KEY (`ingredient_name`,`unit`);

--
-- Indexes for table `ingredient_nutrition`
--
ALTER TABLE `ingredient_nutrition`
  ADD PRIMARY KEY (`ingredient_id`);

--
-- Indexes for table `meal_plans`
--
ALTER TABLE `meal_plans`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_meal_plans_users` (`user_id`);

--
-- Indexes for table `meal_plan_nutrition`
--
ALTER TABLE `meal_plan_nutrition`
  ADD PRIMARY KEY (`id`),
  ADD KEY `meal_plan_recipe_id` (`meal_plan_recipe_id`),
  ADD KEY `member_id` (`member_id`);

--
-- Indexes for table `meal_plan_recipe`
--
ALTER TABLE `meal_plan_recipe`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `meal_plan_recipe_member`
--
ALTER TABLE `meal_plan_recipe_member`
  ADD PRIMARY KEY (`id`),
  ADD KEY `meal_plan_recipe_id` (`meal_plan_recipe_id`),
  ADD KEY `member_id` (`member_id`);

--
-- Indexes for table `meal_plan_recipe_user`
--
ALTER TABLE `meal_plan_recipe_user`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `meal_plan_id` (`meal_plan_id`),
  ADD KEY `meal_plan_recipe_id` (`meal_plan_recipe_id`),
  ADD KEY `recipe_id` (`recipe_id`);

--
-- Indexes for table `recipes`
--
ALTER TABLE `recipes`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_recipes_users` (`user_id`);

--
-- Indexes for table `recipe_ingredients`
--
ALTER TABLE `recipe_ingredients`
  ADD PRIMARY KEY (`id`),
  ADD KEY `recipe_id` (`recipe_id`);

--
-- Indexes for table `recipe_instructions`
--
ALTER TABLE `recipe_instructions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `recipe_id` (`recipe_id`);

--
-- Indexes for table `recipe_nutrition`
--
ALTER TABLE `recipe_nutrition`
  ADD PRIMARY KEY (`id`),
  ADD KEY `recipe_id` (`recipe_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`,`email`);

--
-- Indexes for table `user_exercise`
--
ALTER TABLE `user_exercise`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `exercise_id` (`exercise_id`);

--
-- Indexes for table `user_exercise_member`
--
ALTER TABLE `user_exercise_member`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_user_exercise_member_user_exercise` (`user_exercise_id`),
  ADD KEY `fk_user_exercise_member_members` (`member_id`);

--
-- Indexes for table `user_members`
--
ALTER TABLE `user_members`
  ADD PRIMARY KEY (`member_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `user_preferences`
--
ALTER TABLE `user_preferences`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `user_id` (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `favorite_recipes`
--
ALTER TABLE `favorite_recipes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `grocery_lists`
--
ALTER TABLE `grocery_lists`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=85;

--
-- AUTO_INCREMENT for table `ingredient_nutrition`
--
ALTER TABLE `ingredient_nutrition`
  MODIFY `ingredient_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=224;

--
-- AUTO_INCREMENT for table `meal_plans`
--
ALTER TABLE `meal_plans`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=70;

--
-- AUTO_INCREMENT for table `meal_plan_nutrition`
--
ALTER TABLE `meal_plan_nutrition`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=44;

--
-- AUTO_INCREMENT for table `meal_plan_recipe`
--
ALTER TABLE `meal_plan_recipe`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=94;

--
-- AUTO_INCREMENT for table `meal_plan_recipe_member`
--
ALTER TABLE `meal_plan_recipe_member`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=75;

--
-- AUTO_INCREMENT for table `meal_plan_recipe_user`
--
ALTER TABLE `meal_plan_recipe_user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=65;

--
-- AUTO_INCREMENT for table `recipes`
--
ALTER TABLE `recipes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `recipe_ingredients`
--
ALTER TABLE `recipe_ingredients`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=51;

--
-- AUTO_INCREMENT for table `recipe_instructions`
--
ALTER TABLE `recipe_instructions`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=36;

--
-- AUTO_INCREMENT for table `recipe_nutrition`
--
ALTER TABLE `recipe_nutrition`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=25;

--
-- AUTO_INCREMENT for table `user_exercise`
--
ALTER TABLE `user_exercise`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `user_exercise_member`
--
ALTER TABLE `user_exercise_member`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `user_members`
--
ALTER TABLE `user_members`
  MODIFY `member_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `user_preferences`
--
ALTER TABLE `user_preferences`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `favorite_recipes`
--
ALTER TABLE `favorite_recipes`
  ADD CONSTRAINT `favorite_recipes_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `favorite_recipes_ibfk_2` FOREIGN KEY (`recipe_id`) REFERENCES `recipes` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `grocery_lists`
--
ALTER TABLE `grocery_lists`
  ADD CONSTRAINT `grocery_lists_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `meal_plans`
--
ALTER TABLE `meal_plans`
  ADD CONSTRAINT `fk_meal_plans_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_mealplan_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `meal_plan_nutrition`
--
ALTER TABLE `meal_plan_nutrition`
  ADD CONSTRAINT `meal_plan_nutrition_ibfk_1` FOREIGN KEY (`meal_plan_recipe_id`) REFERENCES `meal_plan_recipe` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `meal_plan_nutrition_ibfk_2` FOREIGN KEY (`member_id`) REFERENCES `user_members` (`member_id`) ON DELETE CASCADE;

--
-- Constraints for table `meal_plan_recipe_member`
--
ALTER TABLE `meal_plan_recipe_member`
  ADD CONSTRAINT `meal_plan_recipe_member_ibfk_1` FOREIGN KEY (`meal_plan_recipe_id`) REFERENCES `meal_plan_recipe` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `meal_plan_recipe_member_ibfk_2` FOREIGN KEY (`member_id`) REFERENCES `user_members` (`member_id`) ON DELETE CASCADE;

--
-- Constraints for table `meal_plan_recipe_user`
--
ALTER TABLE `meal_plan_recipe_user`
  ADD CONSTRAINT `meal_plan_recipe_user_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `meal_plan_recipe_user_ibfk_2` FOREIGN KEY (`meal_plan_id`) REFERENCES `meal_plans` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `meal_plan_recipe_user_ibfk_3` FOREIGN KEY (`meal_plan_recipe_id`) REFERENCES `meal_plan_recipe` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `meal_plan_recipe_user_ibfk_4` FOREIGN KEY (`recipe_id`) REFERENCES `recipes` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `recipes`
--
ALTER TABLE `recipes`
  ADD CONSTRAINT `fk_recipes_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `recipe_ingredients`
--
ALTER TABLE `recipe_ingredients`
  ADD CONSTRAINT `recipe_ingredients_ibfk_1` FOREIGN KEY (`recipe_id`) REFERENCES `recipes` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `recipe_instructions`
--
ALTER TABLE `recipe_instructions`
  ADD CONSTRAINT `recipe_instructions_ibfk_1` FOREIGN KEY (`recipe_id`) REFERENCES `recipes` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `recipe_nutrition`
--
ALTER TABLE `recipe_nutrition`
  ADD CONSTRAINT `recipe_nutrition_ibfk_1` FOREIGN KEY (`recipe_id`) REFERENCES `recipes` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `fk_users_family` FOREIGN KEY (`family_id`) REFERENCES `families` (`family_id`) ON DELETE SET NULL;

--
-- Constraints for table `user_exercise_member`
--
ALTER TABLE `user_exercise_member`
  ADD CONSTRAINT `fk_user_exercise_member_members` FOREIGN KEY (`member_id`) REFERENCES `user_members` (`member_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_user_exercise_member_user_exercise` FOREIGN KEY (`user_exercise_id`) REFERENCES `user_exercise` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `user_members`
--
ALTER TABLE `user_members`
  ADD CONSTRAINT `user_members_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `user_preferences`
--
ALTER TABLE `user_preferences`
  ADD CONSTRAINT `fk_user_preferences_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `user_preferences_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
