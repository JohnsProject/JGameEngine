import bpy
import math
import bmesh

def write(filepath):
	scene = Scene()
	for object in bpy.context.visible_objects:
		object.rotation_mode = 'XYZ'
		if object.type == "CAMERA":
			blenderCamera = bpy.data.cameras[object.name]
			camera = Camera()
			camera.name = blenderCamera.name
			camera.type = blenderCamera.type
			setTransform(camera.transform, object)
			scene.cameras.append(camera)
			
		if object.type == "LAMP":
			blenderLight = bpy.data.lamps[object.name]
			light = Light()
			light.name = blenderLight.name
			light.type = blenderLight.type
			light.strength = blenderLight.energy
			if light.type == "SPOT":
				light.spot = math.degrees(blenderLight.spot_size)
				light.blend = blenderLight.spot_blend
			light.color.red = blenderLight.color[0]
			light.color.green = blenderLight.color[1]
			light.color.blue = blenderLight.color[2]
			light.shadowColor.red = blenderLight.shadow_color[0]
			light.shadowColor.green = blenderLight.shadow_color[1]
			light.shadowColor.blue = blenderLight.shadow_color[2]
			setTransform(light.transform, object)
			scene.lights.append(light)
			
		if object.type == "MESH":
			blenderModel = object.to_mesh(bpy.context.scene, False, "PREVIEW")
			# triangulate model
			bm = bmesh.new()
			bm.from_mesh(blenderModel)
			bmesh.ops.triangulate(bm, faces=bm.faces)
			bm.to_mesh(blenderModel)
			bm.free()
			model = Model()
			model.name = object.name
			setTransform(model.transform, object)
			i = 0
			for blenderVertex in blenderModel.vertices:
				vertex = Vertex()
				vertex.index = i
				vertex.location.append(blenderVertex.co[0])
				vertex.location.append(blenderVertex.co[1])
				vertex.location.append(blenderVertex.co[2])
				vertex.normal.append(blenderVertex.normal[0])
				vertex.normal.append(blenderVertex.normal[1])
				vertex.normal.append(blenderVertex.normal[2])
				model.vertices.append(vertex)
				i += 1
			i = 0
			for blenderFace in blenderModel.polygons:
				face = Face()
				face.index = i
				face.vertices.append(blenderFace.vertices[0])
				face.vertices.append(blenderFace.vertices[1])
				face.vertices.append(blenderFace.vertices[2])
				face.normal.append(blenderFace.normal[0])
				face.normal.append(blenderFace.normal[1])
				face.normal.append(blenderFace.normal[2])
				if blenderModel.uv_layers.active is not None:
					face.uv1.append(blenderModel.uv_layers.active.data[blenderFace.loop_indices[0]].uv[0])
					face.uv1.append(blenderModel.uv_layers.active.data[blenderFace.loop_indices[0]].uv[1])
					face.uv2.append(blenderModel.uv_layers.active.data[blenderFace.loop_indices[1]].uv[0])
					face.uv2.append(blenderModel.uv_layers.active.data[blenderFace.loop_indices[1]].uv[1])
					face.uv3.append(blenderModel.uv_layers.active.data[blenderFace.loop_indices[2]].uv[0])
					face.uv3.append(blenderModel.uv_layers.active.data[blenderFace.loop_indices[2]].uv[1])
				else:
					face.uv1.append(0)
					face.uv1.append(0)
					face.uv2.append(0)
					face.uv2.append(0)
					face.uv3.append(0)
					face.uv3.append(0)
				face.material = blenderFace.material_index
				model.vertices[face.vertices[0]].material = face.material
				model.vertices[face.vertices[1]].material = face.material
				model.vertices[face.vertices[2]].material = face.material
				model.faces.append(face)
				i += 1
			i = 0
			for blenderMaterial in blenderModel.materials:
				material = Material()
				material.index = i
				if blenderMaterial is not None:
					material.name = blenderMaterial.name
					material.color.red = blenderMaterial.diffuse_color[0]
					material.color.green = blenderMaterial.diffuse_color[1]
					material.color.blue = blenderMaterial.diffuse_color[2]
					material.color.alpha = blenderMaterial.alpha
					material.diffuseIntensity = blenderMaterial.diffuse_intensity
					material.specularIntensity = blenderMaterial.specular_intensity
					material.shininess = blenderMaterial.specular_hardness
				model.materials.append(material)
				i += 1
			if len(model.materials) == 0:
				model.materials.append(Material())
			scene.models.append(model)
			bpy.data.meshes.remove(blenderModel)
	writeToFile(filepath, scene)	

def writeToFile(filepath, scene):
	# open target file
	file = open(filepath, "w")
	i = 0
	# write the commons to the file
	commons = "".join(".scene file created by Blender Scene exporter"
			+ "\n" + "https://github.com/JohnsProject/JPGE2" + "\n\n")
	file.write(commons)
	# write the models to the file
	models = ""
	for model in scene.models:
		models += "model<\n"
		models += ("	name<" + model.name + ">name\n")
		models += ("	transform<" + model.transform.toString() + ">transform\n")
		for vertex in model.vertices:
			models += ("	vertex<" + vertex.toString() + ">vertex\n")
		for face in model.faces:
			models += ("	face<" + face.toString() + ">face\n")
		for material in model.materials:
			models += ("	material<" + material.toString() + ">material\n")
		models += ">model\n"
	# write the cameras to the file
	cameras = ""
	for camera in scene.cameras:
		cameras += "camera<\n"
		cameras += ("	name<" + camera.name + ">name\n")
		cameras += ("	type<" + camera.type + ">type\n")
		cameras += ("	transform<" + camera.transform.toString() + ">transform\n")
		cameras += ">camera\n"
	# write the lights to the file
	lights = ""
	for light in scene.lights:
		lights += "light<\n"
		lights += ("	name<" + light.name + ">name\n")
		lights += ("	type<" + light.type + ">type\n")
		lights += ("	strength<" + ("%.3f" % light.strength) + ">strength\n")
		lights += ("	spot<" + ("%.3f" % light.spot) + ">spot\n")
		lights += ("	blend<" + ("%.3f" % light.blend) + ">blend\n")
		lights += ("	color<" + light.color.toString() + ">color\n")
		lights += ("	shadowColor<" + light.shadowColor.toString() + ">shadowColor\n")
		lights += ("	transform<" + light.transform.toString() + ">transform\n")
		lights += ">light\n"
	file.write(models)
	file.write(cameras)
	file.write(lights)
	# close file
	file.close()

def setTransform(transform, object):
	transform.location.append(object.location[0])
	transform.location.append(object.location[1])
	transform.location.append(object.location[2])
	transform.rotation.append(math.degrees(object.rotation_euler[0]))
	transform.rotation.append(math.degrees(object.rotation_euler[1]))
	transform.rotation.append(math.degrees(object.rotation_euler[2]))
	transform.scale.append(object.scale[0])
	transform.scale.append(object.scale[1])
	transform.scale.append(object.scale[2])
	
class Scene:
	def __init__(self):
		self.models = []
		self.cameras = []
		self.lights = []

class Camera:
	def __init__(self):
		self.name = ""
		self.transform = Transform()
		self.type = 0
		
class Light:
	def __init__(self):
		self.name = ""
		self.transform = Transform()
		self.type = 0
		self.color = Color()
		self.shadowColor = Color()
		self.strength = 0
		self.spot = 0
		self.blend = 0
		
class Model:
	def __init__(self):
		self.name = ""
		self.transform = Transform()
		self.vertices = []
		self.faces = []
		self.materials = []
		
class Transform:
	def __init__(self):
		self.location = []
		self.rotation = []
		self.scale = []
	
	def toString(self):
		return str(("%.3f," % self.location[0]) + ("%.3f," % self.location[1]) + ("%.3f," % self.location[2])
				+ ("%.3f," % self.rotation[0]) + ("%.3f," % self.rotation[1]) + ("%.3f," % self.rotation[2])
				+ ("%.3f," % self.scale[0]) + ("%.3f," % self.scale[1]) + ("%.3f" % self.scale[2]))
		
class Color:
	def __init__(self):
		self.red = 0.2
		self.green = 0.2
		self.blue = 0.2
		self.alpha = 1
		
	def toString(self):
		return str(("%.3f," % self.red) + ("%.3f," % self.green) + ("%.3f," % self.blue) + ("%.3f" % self.alpha))
		
class Vertex:
	def __init__(self):
		self.index = 0
		self.location = []
		self.normal = []
		self.material = 0
		
	def toString(self):
		return str(("%.3f," % self.location[0]) + ("%.3f," % self.location[1]) + ("%.3f," % self.location[2])
					+ ("%.3f," % self.normal[0]) + ("%.3f," % self.normal[1]) + ("%.3f," % self.normal[2])
					+ ("%i" % self.material))
		
		
class Face:
	def __init__(self):
		self.index = 0
		self.vertices = []
		self.normal = []
		self.uv1 = []
		self.uv2 = []
		self.uv3 = []
		self.material = 0
		
	def toString(self):
		return str(("%i," % self.vertices[0]) + ("%i," % self.vertices[1]) + ("%i," % self.vertices[2])
					+ ("%.3f," % self.normal[0]) + ("%.3f," % self.normal[1]) + ("%.3f," % self.normal[2])
					+ ("%.3f," % self.uv1[0]) + ("%.3f," % self.uv1[1])
					+ ("%.3f," % self.uv2[0]) + ("%.3f," % self.uv2[1])
					+ ("%.3f," % self.uv3[0]) + ("%.3f," % self.uv3[1])
					+ ("%i" % self.material))
		
class Material:
	def __init__(self):
		self.index = 0
		self.name = ""
		self.color = Color()
		self.diffuseIntensity = 1
		self.specularIntensity = 0.5
		self.shininess = 10
	
	def toString(self):
			return str(self.name + "," + self.color.toString() + ","+ ("%.3f," % self.diffuseIntensity) + ("%.3f," % self.specularIntensity) + ("%.3f" % self.shininess))





