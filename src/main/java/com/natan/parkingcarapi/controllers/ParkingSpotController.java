package com.natan.parkingcarapi.controllers;

import com.natan.parkingcarapi.dtos.ParkingSpotDto;
import com.natan.parkingcarapi.models.ParkingSpotModel;
import com.natan.parkingcarapi.services.ParkingSpotService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {

    final ParkingSpotService parkingSpotService;

    public ParkingSpotController( ParkingSpotService parkingSpotService ) {
        this.parkingSpotService = parkingSpotService;
    }

    @PostMapping
    public ResponseEntity < Object > saveParkingSpot( @RequestBody @Valid ParkingSpotDto parkingSpotDto ) {

        if ( parkingSpotService.existsByLicensePlateCar( parkingSpotDto.getLicensePlateCar( ) ) ) {
            return ResponseEntity.status( HttpStatus.CONFLICT ).body( "Conflict: License plate car is already in use!" );
        }
        if ( parkingSpotService.existsByParkingSpotNumber( parkingSpotDto.getParkingSpotNumber( ) ) ) {
            return ResponseEntity.status( HttpStatus.CONFLICT ).body( "Conflict: Parking spot is already in use" );
        }
        if ( parkingSpotService.existsByApartmentAndBlock( parkingSpotDto.getApartment( ), parkingSpotDto.getBlock( ) ) ) {
            return ResponseEntity.status( HttpStatus.CONFLICT ).body( "Conflict: Parking spot already registered for this apartment/block" );
        }

        var parkingSpotModel = new ParkingSpotModel( );
        BeanUtils.copyProperties( parkingSpotDto, parkingSpotModel );
        parkingSpotModel.setRegistrationDate( String.valueOf( LocalDateTime.now( ZoneId.of( "UTC" ) ) ) );
        return ResponseEntity.status( HttpStatus.CREATED ).body( parkingSpotService.save( parkingSpotModel ) );
    }

    @GetMapping
    public ResponseEntity < Page < ParkingSpotModel > > getAllParkingSpot( @PageableDefault( page = 0, size = 10,  direction = Sort.Direction.ASC, sort = "ui")Pageable pageable ){
        return ResponseEntity.status( HttpStatus.OK ).body( parkingSpotService.findAll(pageable ) );
    }

    @GetMapping( "/{id}" )
    public ResponseEntity < Object > getOneParkingSpot ( @PathVariable( value = "id" ) UUID id ) {

        Optional < ParkingSpotModel > parkingSpotModelOptional = parkingSpotService.findById( id );
        if ( !parkingSpotModelOptional.isPresent( ) ) {
            return ResponseEntity.status( HttpStatus.NOT_FOUND ).body( "Parking spot not found. " );
        }

        return ResponseEntity.status( HttpStatus.OK ).body( parkingSpotModelOptional.get( ) );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object>deleteOneParkingSpot(@PathVariable(value = "id")UUID id ) {
        Optional < ParkingSpotModel > parkingSpotModelOptional = parkingSpotService.findById( id );
        if ( !parkingSpotModelOptional.isPresent( ) ) {
            return ResponseEntity.status( HttpStatus.NOT_FOUND ).body( "Parking spot not found. " );
        }
        parkingSpotService.delete( parkingSpotModelOptional.get( ) );
        return ResponseEntity.status( HttpStatus.OK ).body( "Parking spot deleted successfully" );
    }


    @PutMapping("/{id}")
    public ResponseEntity<Object>updateParkingSpot(@PathVariable(value = "id")UUID id,
                                                   @RequestBody @Valid ParkingSpotDto parkingSpotDto) {
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById( id );
        if(!parkingSpotModelOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body( "Parking spot not found. " );
        }
//        ESSA É UMA DAS MANEIRAS DE FAZER O PUT
//        var parkingSpotModel = parkingSpotModelOptional.get();
//
//        parkingSpotModel.setParkingSpotNumber( parkingSpotDto.getParkingSpotNumber() );
//        parkingSpotModel.setApartment( parkingSpotDto.getApartment() );
//        parkingSpotModel.setModelCar( parkingSpotDto.getModelCar() );
//        parkingSpotModel.setBrandCar( parkingSpotDto.getBrandCar() );
//        parkingSpotModel.setBlock( parkingSpotDto.getBlock() );
//        parkingSpotModel.setColorCar(parkingSpotDto.getColorCar()  );
//        parkingSpotModel.setLicensePlateCar( parkingSpotDto.getLicensePlateCar() );
//        parkingSpotModel.setResponsibleName( parkingSpotDto.getResponsibleName() );

        //SEGUNDA MANEIRA DE FAZER O PUT

        var parkingSpotModel = new ParkingSpotModel();

        BeanUtils.copyProperties( parkingSpotDto, parkingSpotModel );
        parkingSpotModel.setUi(parkingSpotModelOptional.get().getUi());
        parkingSpotModel.setRegistrationDate( parkingSpotModelOptional.get().getRegistrationDate() );
        return ResponseEntity.status(HttpStatus.OK).body( parkingSpotService.save(parkingSpotModel ));
    }
}


