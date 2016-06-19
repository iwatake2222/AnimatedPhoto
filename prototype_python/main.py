# -*- coding: utf-8 -*-
import sys
import os
import math
import cv2
import numpy as np

###
# Const variables
###
NUM_H = 32
NUM_S = 4
NUM_V = 2
GAP_H = math.floor(179/NUM_H)
GAP_S = math.floor(255/NUM_S)
GAP_V = math.floor(255/NUM_V)
BLUR_FILTER_SIZE = 3	# must be odd number
IS_ADD_EDGE = True
IS_ADD_FADE = False
FADE_RATIO = 0.3
###
# Global variables
###


###
# Main
###
def main():
	if(len(sys.argv) != 2):
		print 'Usage: \n# python %s filename' % sys.argv[0]
		quit()
	inputFilename = sys.argv[1]
	print '... processing  %s' % inputFilename
	inputImage = cv2.imread(inputFilename)
	cv2.imshow('inputImage', inputImage)
	outputImage = animeFilter(inputImage)
	outputFilename = "conv_" + os.path.splitext(inputFilename)[0] + ".jpg"
	cv2.imwrite(outputFilename, outputImage)
	cv2.imshow('outputImage', outputImage)
	cv2.waitKey()


###
# Filters
###
def animeFilter(inputImage):
	height = inputImage.shape[0]
	width = inputImage.shape[1]
	outputImage = np.zeros((height, width, 3), np.uint8)
	# inputImage = cv2.GaussianBlur(inputImage, (BLUR_FILTER_SIZE, BLUR_FILTER_SIZE), 0)
	inputImage = cv2.medianBlur(inputImage, BLUR_FILTER_SIZE)
	hsvImage = cv2.cvtColor(inputImage, cv2.COLOR_BGR2HSV)
	grayImage = cv2.cvtColor(inputImage, cv2.COLOR_BGR2GRAY)

	for h in range(NUM_H):
		for s in range(NUM_S):
			for v in range(NUM_V):
				filterPrm = createHSVRange(h,s,v)
				# print filterPrm
				lowerBlue = np.array(filterPrm[0], dtype=np.uint8)
				upperBlue = np.array(filterPrm[1], dtype=np.uint8)
				mask = cv2.inRange(hsvImage, lowerBlue, upperBlue)
				# cv2.imshow('mask', mask)
				filledImage = np.tile(np.uint8(hsv2bgr(filterPrm[2])), (height, width, 1))
				tempImage = cv2.bitwise_or(filledImage, filledImage, mask=mask)
				outputImage = cv2.bitwise_or(outputImage, tempImage)
				# cv2.imshow('filledImage', filledImage)
		# cv2.imshow('outputImage', outputImage)
		# cv2.waitKey()
	if IS_ADD_EDGE == True:
		edgeImage = cv2.Canny(grayImage, 50, 150)
		edgeImage = cv2.bitwise_not(edgeImage)
		# cv2.imshow('edgeImage', edgeImage)
		edgeImage = cv2.cvtColor(edgeImage, cv2.COLOR_GRAY2BGR)
		outputImage = cv2.bitwise_and(outputImage, edgeImage)
	if IS_ADD_FADE == True:
		ret, fadeImage = cv2.threshold(grayImage,100,255,cv2.THRESH_BINARY)
		fadeImage = cv2.cvtColor(fadeImage, cv2.COLOR_GRAY2BGR)
		# cv2.imshow('fadeImage', fadeImage)
		outputImage = cv2.addWeighted(outputImage,1-FADE_RATIO,fadeImage,FADE_RATIO,0)
	return outputImage


def createHSVRange(h, s, v):
	# take care of calculation error
	hHigh = GAP_H*(h+1)-1 if h != NUM_H-1 else 179
	sHigh = GAP_S*(s+1)-1 if s != NUM_S-1 else 255
	vHigh = GAP_V*(v+1)-1 if v != NUM_V-1 else 255
	return ((GAP_H*h, GAP_S*s, GAP_V*v), (hHigh, sHigh, vHigh), (GAP_H*(h+0.5), GAP_S*(s+0.5), GAP_V*(v+0.5)))


def copyImage(inputImage):
	height = inputImage.shape[0]
	width = inputImage.shape[1]
	outputImage = np.zeros((height, width, 3), np.uint8)
	for y in xrange(height):
		for x in xrange(width):
			outputImage[y, x] = inputImage[y, x]
	return outputImage


def animeFilter2(inputImage):
	grayImage = cv2.cvtColor(inputImage, cv2.COLOR_BGR2GRAY)
	filteredImage = cv2.GaussianBlur(grayImage, (5, 5), 0)
	# cv2.imshow('filteredImage', filteredImage)
	ret, threshImage = cv2.threshold(filteredImage, 127, 255, cv2.THRESH_BINARY)
	# cv2.imshow('threshImage', threshImage)
	# edgeImage = cv2.adaptiveThreshold(filteredImage,255,cv2.ADAPTIVE_THRESH_MEAN_C, cv2.THRESH_BINARY,11,2)
	edgeImage = cv2.adaptiveThreshold(filteredImage,255,cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY,11,2)
	# cv2.imshow('edgeImage', edgeImage)
	andImage = cv2.bitwise_and(filteredImage, edgeImage)
	# cv2.imshow('andImage', andImage)
	andImage2 = cv2.cvtColor(andImage, cv2.COLOR_GRAY2BGR)
	# cv2.imshow('andImage2', andImage2)
	inputImage = cv2.GaussianBlur(inputImage, (5, 5), 0)
	outputImage = cv2.bitwise_and(andImage2, inputImage)
	# cv2.imshow('outputImage', outputImage)
	return outputImage


###
# Utilities
###
def hsv2rgb(h, s, v):
	bgr = cv2.cvtColor(np.array([[[h, s, v]]], dtype=np.uint8), cv2.COLOR_HSV2BGR)[0][0]
	return (bgr[2], bgr[1], bgr[0])


def hsv2bgr(h, s, v):
	bgr = cv2.cvtColor(np.array([[[h, s, v]]], dtype=np.uint8), cv2.COLOR_HSV2BGR)[0][0]
	return (bgr[0], bgr[1], bgr[1])


def hsv2bgr(hsv):
	bgr = cv2.cvtColor(np.array([[hsv]], dtype=np.uint8), cv2.COLOR_HSV2BGR)[0][0]
	return (bgr[0], bgr[1], bgr[2])


def rgb2hsv(r, g, b):
	hsv = cv2.cvtColor(np.array([[[b, g, r]]], dtype=np.uint8), cv2.COLOR_BGR2HSV)[0][0]
	return (hsv[0], hsv[1], hsv[2])


###
# Main
###
if __name__ == "__main__":
	main()
