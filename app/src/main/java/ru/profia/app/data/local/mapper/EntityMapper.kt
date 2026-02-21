package ru.profia.app.data.local.mapper

import ru.profia.app.data.model.OpeningData
import ru.profia.app.data.model.OpeningType
import ru.profia.app.data.model.ProjectData
import ru.profia.app.data.model.RoomData
import ru.profia.app.data.model.RoomFormData
import ru.profia.app.data.local.entity.OpeningEntity
import ru.profia.app.data.local.entity.ProjectEntity
import ru.profia.app.data.local.entity.RoomEntity

fun ProjectEntity.toProjectData(rooms: List<RoomData>): ProjectData = ProjectData(
    id = id,
    lastName = lastName,
    firstName = firstName,
    middleName = middleName,
    email = email,
    phone = phone,
    address = address,
    city = city,
    street = street,
    house = house,
    apartment = apartment,
    rooms = rooms.toMutableList(),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun RoomEntity.toRoomFormData(): RoomFormData = RoomFormData(
    name = name,
    length = length,
    width = width,
    height = height,
    floorAreaOverride = floorArea,
    ceilingAreaOverride = ceilingArea,
    wallAreaOverride = wallArea,
    hasSlopes = hasSlopes,
    slopesLength = slopesLength,
    hasBoxes = hasBoxes,
    boxesLength = boxesLength
)

fun RoomEntity.toRoomData(openings: List<OpeningData>): RoomData = RoomData(
    id = id,
    name = name,
    length = length,
    width = width,
    height = height,
    floorArea = floorArea,
    wallArea = wallArea,
    ceilingArea = ceilingArea,
    perimeter = perimeter,
    hasSlopes = hasSlopes,
    slopesLength = slopesLength,
    hasBoxes = hasBoxes,
    boxesLength = boxesLength,
    openings = openings,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun OpeningEntity.toOpeningData(): OpeningData = OpeningData(
    id = id,
    type = OpeningType.valueOf(type),
    width = width,
    height = height,
    count = count
)
